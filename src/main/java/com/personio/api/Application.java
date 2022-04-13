package com.personio.api;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.personio.api.auth.AuthController;
import com.personio.api.hierarchy.HierarchyController;
import com.personio.api.hierarchy.model.Employee;
import com.personio.api.hierarchy.model.Hierarchy;
import com.personio.api.utils.Constants;
import com.personio.api.utils.RequestErrorException;
import com.personio.api.utils.ResponseError;
import org.eclipse.jetty.http.HttpStatus;

import java.sql.SQLException;

import static com.personio.api.utils.JsonUtil.json;
import static com.personio.api.utils.JsonUtil.toJson;
import static spark.Spark.*;

public class Application {
    public static final ConnectionSource connectionSource;
    private static final String JDBC_H2_MEM = "jdbc:h2:mem:account";

    static {
        try {
            connectionSource = new JdbcConnectionSource(JDBC_H2_MEM);
            TableUtils.createTable(connectionSource, Hierarchy.class);
            TableUtils.createTable(connectionSource, Employee.class);
        } catch (SQLException e) {
            throw new RequestErrorException(HttpStatus.INTERNAL_SERVER_ERROR_500, e.getMessage());
        }
    }


    public static void main(String[] args) {

        // -- Check the authentication
        before((req, res) -> AuthController.validateLogin(req));

        // -- Set proper content-type to all responses
        after((req, res) -> res.type(Constants.STANDARD_RESPONSE_CONTENTTYPE));

        get("/hierarchy/supervisor-supervisor", HierarchyController.getSupervisorAndSupervisorByEmployee, json());
        post("/hierarchy", Constants.STANDARD_RESPONSE_CONTENTTYPE, HierarchyController.save);
        post("/login", Constants.STANDARD_RESPONSE_CONTENTTYPE, AuthController.login, json());

        exception(RequestErrorException.class, (e, req, res) -> {
            res.type(Constants.STANDARD_RESPONSE_CONTENTTYPE);
            res.status(e.getStatusCode());
            res.body(toJson(new ResponseError(e)));
        });

    }
}
