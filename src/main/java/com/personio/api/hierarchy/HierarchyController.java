package com.personio.api.hierarchy;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.personio.api.auth.AuthController;
import com.personio.api.hierarchy.model.Employee;
import com.personio.api.hierarchy.model.Hierarchy;
import com.personio.api.utils.RequestUtil;
import com.personio.api.utils.RequestErrorException;
import org.eclipse.jetty.http.HttpStatus;
import spark.Request;
import spark.Response;
import spark.Route;

import java.sql.SQLException;


public class HierarchyController {
    private static final HierarchyService hierarchyService;

    private static final String JDBC_H2_MEM = "jdbc:h2:mem:account";
    private static final String EMPTY_BODY = "Empty body";
    private static final String EMPTY_NAME = "Empty name";

    private HierarchyController() {
    }

    static {
        try {
            ConnectionSource connectionSource = new JdbcConnectionSource(JDBC_H2_MEM);
            TableUtils.createTable(connectionSource, Hierarchy.class);
            TableUtils.createTable(connectionSource, Employee.class);
            Dao<Hierarchy, String> hierarchyDao = DaoManager.createDao(connectionSource, Hierarchy.class);
            Dao<Employee, String> employeeDao = DaoManager.createDao(connectionSource, Employee.class);
            hierarchyService = new HierarchyService(hierarchyDao, employeeDao);
        } catch (SQLException e) {
            throw new RequestErrorException(HttpStatus.INTERNAL_SERVER_ERROR_500, e.getMessage());
        }
    }

    public static final Route save = (Request request, Response response) -> {
        AuthController.validateContentType(request);

        String body = request.body();
        if (body.isEmpty()) {
            throw new RequestErrorException(HttpStatus.BAD_REQUEST_400, EMPTY_BODY);
        }
        return hierarchyService.save(body).toExpectedJson();
    };

    public static final Route getSupervisorAndSupervisorByEmployee = (Request request, Response response) -> {
        String name = RequestUtil.getQueryName(request);
        if (name.isEmpty()) {
            throw new RequestErrorException(HttpStatus.BAD_REQUEST_400, EMPTY_NAME);
        }
        return hierarchyService.getSupervisorAndSupervisorByName(name);
    };


}
