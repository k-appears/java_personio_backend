package com.personio.api.hierarchy;

import com.j256.ormlite.dao.Dao;
import com.personio.api.hierarchy.model.Employee;
import com.personio.api.hierarchy.model.Hierarchy;
import com.personio.api.utils.RequestErrorException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class HierarchyServiceTest {

    private static final Dao<Hierarchy, String> hierarchyMock = Mockito.mock(Dao.class);
    private static final Dao<Employee, String> employeeDao = Mockito.mock(Dao.class);
    static HierarchyService hierarchyService = new HierarchyService(hierarchyMock, employeeDao);


    @Test
    void getHierarchyByName_nameNotFound() {
        assertThrows(RequestErrorException.class, () -> hierarchyService.save("invalid body"));
    }

    @Test
    void save_invalidBody() {
        assertThrows(RequestErrorException.class, () -> hierarchyService.save("invalid body"));
    }

    @Test
    void save_emptyBody() {
        assertThrows(RequestErrorException.class, () -> hierarchyService.save("{}"));
    }

    @Test
    void save_employeeNull() {
        assertThrows(RequestErrorException.class, () -> hierarchyService.save("{'Hello':null}"));
    }

    @Test
    void save_employeeEmpty() {
        assertThrows(RequestErrorException.class, () -> hierarchyService.save("{'Hello':''}"));
    }

    @Test
    void save_containsLoop() {
        String body = "{'Jonas': 'Nick', 'Barbara': 'Nick', 'Nick': 'Sophie', 'Sophie': 'Jonas'}";
        assertThrows(RequestErrorException.class, () -> hierarchyService.save(body));
    }

    @Test
    void save_multipleRootsDuplicated() {
        String body = "{\n" +
                "          \"Pete\": \"Nick\",\n" +
                "          \"Barbara\": \"Nick\",\n" +
                "          \"Nick\": \"Sophie\",\n" +
                "          \"Sophie\": \"Jonas\", \n" +
                "          \"Sophie\": \"OtherRoot\" \n" +
                "}";
        RequestErrorException aThrows = assertThrows(RequestErrorException.class, () -> hierarchyService.save(body));
        assertTrue(aThrows.getMessage().contains("duplicate key: Sophie"));
    }

    @Test
    void save_multipleRoots() {
        String body = "{\n" +
                "          \"Pete\": \"Nick\", \n" +
                "          \"Barbara\": \"Nick\", \n" +
                "          \"Nick\": \"Sophie\", \n" +
                "          \"Sophie\": \"Jonas\", \n" +
                "          \"Mickey\": \"Mouse\" \n" +
                "}";
        RequestErrorException aThrows = assertThrows(RequestErrorException.class, () -> hierarchyService.save(body));
        assertEquals("Found multiple roots", aThrows.getMessage());
    }

    @Test
    void merge_zeroElement() {
        Map<String, Set<String>> result = hierarchyService.merge(Collections.emptyList());

        assertEquals(Collections.emptyMap(), result);
    }

    @Test
    void merge_oneElement() {
        Map<String, Set<String>> result = hierarchyService.merge(List.of(Map.of("hello", "")));

        assertEquals(Map.of("hello", Set.of("")), result);
    }

    @Test
    void merge_twoElements() {
        Map<String, Set<String>> result = hierarchyService.merge(List.of(
                Map.of("hello", "kitty"),
                Map.of("hello", "mitty")));

        assertEquals(Map.of("hello", Set.of("kitty", "mitty")), result);
    }

    @Test
    void findPath_noCycle() {
        Map<String, Set<String>> mergedPaths = Map.of("hello", Set.of("kitty", "mitty"), "mitty", Set.of("witty"));
        Map.Entry<String, Set<String>> entry = Map.entry("hello", Set.of("kitty", "mitty"));

        Set<String> path = hierarchyService.findPath(mergedPaths, entry);

        assertEquals(Set.of("hello", "mitty", "kitty", "witty"), path);
    }

    @Test
    void findPath_withCycle() {
        Map<String, Set<String>> mergedPaths = Map.of("hello", Set.of("kitty", "mitty"), "mitty", Set.of("hello"));
        Map.Entry<String, Set<String>> entry = Map.entry("hello", Set.of("kitty", "mitty"));

        assertThrows(RequestErrorException.class, () -> hierarchyService.findPath(mergedPaths, entry));
    }

    @Test
    void findHeadOfSupervisors_emptyParameter() {
        Map<String, Set<String>> mergedPaths = Map.of();
        String head = hierarchyService.findHeadOfSupervisors(mergedPaths);

        assertEquals("", head);
    }

    @Test
    void findHeadOfSupervisors_empty() {
        Map<String, Set<String>> mergedPaths = Map.of("Sophie", Set.of(), "Nick", Set.of(), "Jonas", Set.of());
        String head = hierarchyService.findHeadOfSupervisors(mergedPaths);

        assertEquals("", head);
    }


    @Test
    void findHeadOfSupervisors_multipleHeads() {
        Map<String, Set<String>> mergedPaths = Map.of("Sophie", Set.of(), "Nick", Set.of(), "Jonas", Set.of());
        String head = hierarchyService.findHeadOfSupervisors(mergedPaths);

        assertEquals("", head);
    }


    @Test
    void getSupervisorAndSupervisorByName_notFound() {
        assertThrows(RequestErrorException.class, () -> hierarchyService.getSupervisorAndSupervisorByName("NotFound"));
    }

    @Test
    void getSupervisorAndSupervisorByName_notFoundSup() throws SQLException {
        Hierarchy hierarchy = Mockito.mock(Hierarchy.class, Mockito.RETURNS_DEEP_STUBS);
        when(hierarchy.getParent().getName()).thenReturn("ParentName");
        when(hierarchyMock.queryForId(anyString())).thenReturn(hierarchy).thenReturn(null);
        assertThrows(RequestErrorException.class, () -> hierarchyService.getSupervisorAndSupervisorByName("FoundSupNotFoundSupSup"));
    }

    @Test
    void getSupervisorAndSupervisorByName_notFoundSupSup() throws SQLException {
        Hierarchy hierarchy = Mockito.mock(Hierarchy.class, Mockito.RETURNS_DEEP_STUBS);
        when(hierarchy.getParent().getName()).thenReturn("ParentName");
        when(hierarchyMock.queryForId(anyString())).thenReturn(hierarchy).thenReturn(Mockito.mock(Hierarchy.class));
        assertThrows(RequestErrorException.class, () -> hierarchyService.getSupervisorAndSupervisorByName("FoundSupNotFoundSupSup"));
    }

}