package integration;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.personio.api.auth.AuthController;
import com.personio.api.hierarchy.HierarchyController;
import com.personio.api.utils.Constants;
import com.personio.api.utils.JsonUtil;
import com.personio.api.utils.RequestErrorException;
import com.personio.api.utils.ResponseError;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Service;

import java.sql.SQLException;
import java.util.Set;

import static com.personio.api.utils.JsonUtil.toJson;
import static integration.Util.performGet;
import static integration.Util.performPost;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static spark.Service.ignite;

class ApplicationIT {
    private static final Logger log = LoggerFactory.getLogger(ApplicationIT.class);


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
        http.get("/hierarchy/get_sup", HierarchyController.getSupervisorAndSupervisorByEmployee);
        http.post("/hierarchy/create", Constants.STANDARD_RESPONSE_CONTENTTYPE, HierarchyController.save);

        http.exception(RequestErrorException.class, (e, req, res) -> {
            res.status(e.getStatusCode());
            res.body(toJson(new ResponseError(e)));
        });
        http.awaitInitialization();
        log.info("Spark started");
    }

    @AfterEach
    void shutdown() throws SQLException {
//        ConnectionSource connectionSource = new JdbcConnectionSource(Constants.JDBC_H2_MEM);
//        TableUtils.clearTable(connectionSource, Hierarchy.class);
//        TableUtils.clearTable(connectionSource, Employee.class);
        log.info("Stopping Spark");
        http.stop();
    }

    @Test
    void save_no_name() {
        String result = performPost("/hierarchy/create", "");
        assertTrue(result.contains("Empty body"));
    }

    @Test
    void save_nameNick() {
        String resultPost = performPost("/hierarchy/create", body);
        JsonObject jsonObject = new Gson().fromJson(resultPost, JsonObject.class);
        assertTrue(jsonObject.has("Jonas"));
        assertEquals(1, jsonObject.size());
        JsonObject jonas = (JsonObject) jsonObject.get("Jonas");
        assertTrue(jonas.has("Sophie"));
        JsonObject sophie = (JsonObject) jonas.get("Sophie");
        assertTrue(sophie.has("Nick"));
        JsonObject nick = (JsonObject) sophie.get("Nick");
        assertTrue(nick.has("Barbara"));
        assertTrue(nick.has("Pete"));
    }

    @Test
    void getSupervisorAndSupervisorByEmployee() {
        performPost("/hierarchy/create", body);
        String resultGet = performGet("?name=Nick");
        Set<String> result = JsonUtil.fromStringToObject(resultGet, new TypeToken<Set<String>>() {
        }.getType());
        assertEquals(result, Set.of("Sophie", "Jonas"));
    }

    @Test
    void getSupAndSup_noSup() {
        performPost("/hierarchy/create", body);
        String resultGet = performGet("?name=Jonas");
        ResponseError responseError = JsonUtil.fromStringToObject(resultGet, ResponseError.class);
        assertEquals("Not found parent of Jonas", responseError.getMessage());
    }

    @Test
    void get_NotFoundName() {
        performPost("/hierarchy/create", body);
        String resultGet = performGet("?name=AnythingNotInBody");
        ResponseError responseError = JsonUtil.fromStringToObject(resultGet, ResponseError.class);
        assertEquals("Not found AnythingNotInBody", responseError.getMessage());
    }

    @Test
    void main_nameNick_loop() {
        String bodyLoop = "{\"Milky\": \"Way\", \"Way\": \"Solar\", \"Solar\": \"Milky\"}";
        String postResult = performPost("/hierarchy/create", bodyLoop);
        ResponseError responseError = JsonUtil.fromStringToObject(postResult, ResponseError.class);
        assertEquals("Cycle found", responseError.getMessage());
    }

}
