package com.personio.api;

import com.personio.api.auth.AuthController;
import com.personio.api.hierarchy.HierarchyController;
import com.personio.api.utils.Constants;
import com.personio.api.utils.RequestErrorException;
import com.personio.api.utils.ResponseError;

import static com.personio.api.utils.JsonUtil.json;
import static com.personio.api.utils.JsonUtil.toJson;
import static spark.Spark.*;

public class Application {


    public static void main(String[] args) {

        // -- Check the authentication
        before((req, res) -> AuthController.validateLogin(req));

        // -- Set proper content-type to all responses
        after((req, res) -> res.type(Constants.STANDARD_RESPONSE_CONTENTTYPE));

        get("/hierarchy/get_sup", HierarchyController.getSupervisorAndSupervisorByEmployee);
        post("/hierarchy/create", Constants.STANDARD_RESPONSE_CONTENTTYPE, HierarchyController.save);
        post("/login", Constants.STANDARD_RESPONSE_CONTENTTYPE, AuthController.login, json());

        exception(RequestErrorException.class, (e, req, res) -> {
            res.status(e.getStatusCode());
            res.body(toJson(new ResponseError(e)));
        });

    }
}
