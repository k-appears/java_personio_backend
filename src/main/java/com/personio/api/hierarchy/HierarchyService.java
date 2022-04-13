package com.personio.api.hierarchy;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.table.TableUtils;
import com.personio.api.hierarchy.model.Employee;
import com.personio.api.hierarchy.model.Hierarchy;
import com.personio.api.utils.RequestErrorException;
import com.personio.api.utils.RequestUtil;
import com.personio.api.utils.ResponseSupervisor;
import org.eclipse.jetty.http.HttpStatus;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.personio.api.Application.connectionSource;

public class HierarchyService {
    private static final String RELATIONSHIP_NOT_FOUND = "Relationship not found";
    private static final String CYCLE_FOUND = "Cycle found, employee %s in already processed %s";
    private static final String NOT_FOUND = "Not found ";
    private static final String NOT_FOUND_PARENT_OF = "Not found parent of ";
    private static final String NOT_FOUND_SUPERVISOR_OF = "Not found supervisor of '%s'";
    private static final String NOT_FOUND_PARENT_SUPERVISOR_OF = "Supervisor of '%s' is '%s' but not found supervisor of '%s'";
    private final Dao<Hierarchy, String> hierarchyDao;
    private final Dao<Employee, String> employeeDao;


    public HierarchyService(Dao<Hierarchy, String> hierarchyDao, Dao<Employee, String> employeeDao) {
        this.hierarchyDao = hierarchyDao;
        this.employeeDao = employeeDao;
    }

    public ResponseSupervisor getSupervisorAndSupervisorByName(String name) {
        try {
            Hierarchy hierarchyFirst = hierarchyDao.queryForId(name);
            if (hierarchyFirst == null) {
                throw new RequestErrorException(HttpStatus.NOT_FOUND_404, NOT_FOUND + name);
            }
            if (hierarchyFirst.getParent() == null) {
                throw new RequestErrorException(HttpStatus.NOT_FOUND_404, NOT_FOUND_PARENT_OF + name);
            }
            String supervisor = hierarchyFirst.getParent().getName();
            Hierarchy hierarchyParent = hierarchyDao.queryForId(supervisor);
            if (hierarchyParent == null) {
                throw new RequestErrorException(HttpStatus.NOT_FOUND_404, String.format(NOT_FOUND_SUPERVISOR_OF, supervisor));
            }
            if (hierarchyParent.getParent() == null) {
                throw new RequestErrorException(HttpStatus.NOT_FOUND_404,
                        String.format(NOT_FOUND_PARENT_SUPERVISOR_OF, name, supervisor, supervisor));
            }
            return new ResponseSupervisor(supervisor, hierarchyParent.getParent().getName());
        } catch (SQLException e) {
            throw new RequestErrorException(HttpStatus.INTERNAL_SERVER_ERROR_500, e.getMessage());
        }
    }

    public Hierarchy save(String requestBody) {
        Map<String, String> relationship = RequestUtil.validateBody(requestBody);

        Map<String, Set<String>> mergedPaths = createPaths(relationship);

        String head = findHeadOfSupervisors(mergedPaths);
        if (moreThanOneRoot(head, mergedPaths)) {
            throw new RequestErrorException(HttpStatus.BAD_REQUEST_400, "Found multiple roots");
        }

        Hierarchy hierarchy = createHierarchy(mergedPaths, head);

        try {
            TableUtils.clearTable(connectionSource, Hierarchy.class);
            TableUtils.clearTable(connectionSource, Employee.class);
            persist(hierarchy);
        } catch (SQLException e) {
            throw new RequestErrorException(HttpStatus.INTERNAL_SERVER_ERROR_500, e.getMessage());
        }
        return hierarchy;
    }


    private void persist(Hierarchy hierarchy) throws SQLException {
        if (hierarchy == null) {
            return;
        }
        for (Employee employee : hierarchy.getEmployees()) {
            persist(employee.getHierarchy());
            employeeDao.createIfNotExists(employee);
        }
        hierarchyDao.createIfNotExists(hierarchy);
    }

    String findHeadOfSupervisors(Map<String, Set<String>> mergedPaths) {
        Set<String> processed = new HashSet<>();
        int longestPath = 1;
        String headOfSupervisors = "";
        while (processed.size() < combineKeysAndValues(mergedPaths).size()) {
            Map.Entry<String, Set<String>> entry = findNotProcessed(mergedPaths, processed);
            Set<String> processedInPath = findPath(mergedPaths, entry);
            if (processedInPath.size() > longestPath) {
                longestPath = processedInPath.size();
                headOfSupervisors = entry.getKey();
            }
            processed.addAll(processedInPath);
        }
        return headOfSupervisors;
    }

    private boolean moreThanOneRoot(String head, Map<String, Set<String>> mergedPaths) {
        Map.Entry<String, Set<String>> entry = Map.entry(head, mergedPaths.get(head));
        Set<String> processedInPath = findPath(mergedPaths, entry);
        try {
            findNotProcessed(mergedPaths, processedInPath);
            return true;
        } catch (RequestErrorException e) {
            return false;
        }
    }

    Set<String> findPath(Map<String, Set<String>> mergedPaths, Map.Entry<String, Set<String>> entry) {
        Queue<String> queue = new ArrayDeque<>(entry.getValue());
        Set<String> localProcessed = new HashSet<>();
        localProcessed.add(entry.getKey());
        while (!queue.isEmpty()) {
            String supervisor = queue.remove();
            if (localProcessed.contains(supervisor)) {
                throw new RequestErrorException(HttpStatus.BAD_REQUEST_400, String.format(CYCLE_FOUND, supervisor, localProcessed));
            }
            localProcessed.add(supervisor);
            if (mergedPaths.containsKey(supervisor)) {
                queue.addAll(mergedPaths.get(supervisor));
            }
        }
        return localProcessed;
    }

    private Map.Entry<String, Set<String>> findNotProcessed(Map<String, Set<String>> mergedPaths, Set<String> processed) {
        return mergedPaths.entrySet().stream()
                .filter(ent -> !processed.contains(ent.getKey()))
                .findFirst().orElseThrow(() -> new RequestErrorException(HttpStatus.BAD_REQUEST_400, RELATIONSHIP_NOT_FOUND));
    }

    private Set<String> combineKeysAndValues(Map<String, Set<String>> mergedPaths) {
        return Stream.concat(mergedPaths.keySet().stream(),
                        mergedPaths.values().stream().flatMap(Collection::stream))
                .collect(Collectors.toSet());
    }

    private Hierarchy createHierarchy(Map<String, Set<String>> mergedPaths, String head) {
        return new Hierarchy(head, mergedPaths.get(head), mergedPaths, null);
    }


    Map<String, Set<String>> merge(List<Map<String, String>> paths) {
        return paths.stream()
                .flatMap(m -> m.entrySet().stream())
                .collect(Collectors.groupingBy(Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toSet())));
    }

    /**
     * @param relationship from flat input data
     *                     Example `A -> B, B -> C, C -> D`
     * @return list of traversable paths
     * Example
     * `A -> B, B -> C, C -> D`
     * `A -> B, B -> C, C -> E`
     */
    private Map<String, Set<String>> createPaths(Map<String, String> relationship) {
        return relationship.entrySet()
                .stream()
                .collect(Collectors.groupingBy(Map.Entry::getValue,
                        Collectors.mapping(Map.Entry::getKey, Collectors.toSet())));
    }

}
