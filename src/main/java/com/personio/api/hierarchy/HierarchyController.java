package com.personio.api.hierarchy;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.personio.api.auth.AuthController;
import com.personio.api.hierarchy.model.Employee;
import com.personio.api.hierarchy.model.Hierarchy;
import com.personio.api.utils.RequestErrorException;
import com.personio.api.utils.RequestUtil;
import org.eclipse.jetty.http.HttpStatus;
import spark.Request;
import spark.Response;
import spark.Route;

import java.sql.SQLException;

import static com.personio.api.Application.connectionSource;


public class HierarchyController {
    private static final HierarchyService hierarchyService;
    static {
        try {
            Dao<Hierarchy, String> hierarchyDao = DaoManager.createDao(connectionSource, Hierarchy.class);
            Dao<Employee, String> employeeDao = DaoManager.createDao(connectionSource, Employee.class);
            hierarchyService = new HierarchyService(hierarchyDao, employeeDao);
        } catch (SQLException e) {
            throw new RequestErrorException(HttpStatus.INTERNAL_SERVER_ERROR_500, e.getMessage());
        }
    }

    private static final String EMPTY_BODY = "Empty body";
    public static final Route save = (Request request, Response response) -> {
        AuthController.validateContentType(request);

        String body = request.body();
        if (body.isEmpty()) {
            throw new RequestErrorException(HttpStatus.BAD_REQUEST_400, EMPTY_BODY);
        }
        return hierarchyService.save(body).toExpectedJson();
    };
    private static final String EMPTY_NAME = "Empty name";
    public static final Route getSupervisorAndSupervisorByEmployee = (Request request, Response response) -> {
        String name = RequestUtil.getQueryName(request);
        if (name.isEmpty()) {
            throw new RequestErrorException(HttpStatus.BAD_REQUEST_400, EMPTY_NAME);
        }
        return hierarchyService.getSupervisorAndSupervisorByName(name);
    };

    private HierarchyController() {
    }


}
