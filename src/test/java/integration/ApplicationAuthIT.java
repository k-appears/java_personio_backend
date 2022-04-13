package integration;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.personio.api.auth.AuthController;
import com.personio.api.auth.model.UserLogin;
import com.personio.api.hierarchy.HierarchyController;
import com.personio.api.utils.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Service;

import static com.personio.api.utils.JsonUtil.json;
import static com.personio.api.utils.JsonUtil.toJson;
import static integration.Util.performGet;
import static integration.Util.performPost;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static spark.Service.ignite;

class ApplicationAuthIT {
    private static final Logger log = LoggerFactory.getLogger(ApplicationAuthIT.class);

    private final Service http = ignite();
    String body = "{\n" +
            "          \"Pete\": \"Nick\",\n" +
            "          \"Barbara\": \"Nick\",\n" +
            "          \"Nick\": \"Sophie\",\n" +
            "          \"Sophie\": \"Jonas\"\n" +
            "}";

    @BeforeEach
    void setup() {
        http.init();
        http.before((req, res) -> AuthController.validateLogin(req));
        http.get("/hierarchy/supervisor-supervisor", HierarchyController.getSupervisorAndSupervisorByEmployee, json());
        http.post("/hierarchy", Constants.STANDARD_RESPONSE_CONTENTTYPE, HierarchyController.save);
        http.post("/login", Constants.STANDARD_RESPONSE_CONTENTTYPE, AuthController.login);

        http.exception(RequestErrorException.class, (e, req, res) -> {
            res.status(e.getStatusCode());
            res.body(toJson(new ResponseError(e)));
        });
        http.awaitInitialization();
        log.info("Spark started");
    }

    @AfterEach
    void shutdown() {
        log.info("Stopping Spark");
        http.stop();
    }


    @Test
    void save_no_auth() {
        String postResult = performPost("/hierarchy", "");
        ResponseError responseError = JsonUtil.fromStringToObject(postResult, ResponseError.class);
        assertEquals("Invalid/expired authentication", responseError.getMessage());
    }

    @Test
    void login_empty_body() {
        String postResult = performPost("/login", "");
        ResponseError responseError = JsonUtil.fromStringToObject(postResult, ResponseError.class);
        assertEquals("Required username and password in body", responseError.getMessage());
    }

    @Test
    void login_invalid_username_password() {
        UserLogin userLogin = new UserLogin("invalid", "invalid");
        String postResult = performPost("/login", JsonUtil.toJson(userLogin));
        ResponseError responseError = JsonUtil.fromStringToObject(postResult, ResponseError.class);
        assertEquals("Username or password invalid", responseError.getMessage());
    }

    @Test
    void save_valid_username_password() {
        UserLogin userLogin = new UserLogin("test", "test");
        String token = performPost("/login", JsonUtil.toJson(userLogin));

        String postResult = performPost("/hierarchy?token=" + token, body);
        JsonObject jsonObject = new Gson().fromJson(postResult, JsonObject.class);
        assertTrue(jsonObject.has("Jonas"));
        // Rest of the body validation in ApplicationIT
    }

    @Test
    void getSupervisorAndSupervisorByEmployee() {
        UserLogin userLogin = new UserLogin("test", "test");
        String token = performPost("/login", JsonUtil.toJson(userLogin));
        performPost("/hierarchy?token=" + token, body);
        String resultGet = performGet("?name=Nick&token=" + token);
        ResponseSupervisor result = JsonUtil.fromStringToObject(resultGet, ResponseSupervisor.class);
        assertEquals(result, new ResponseSupervisor("Sophie", "Jonas"));
    }

}
