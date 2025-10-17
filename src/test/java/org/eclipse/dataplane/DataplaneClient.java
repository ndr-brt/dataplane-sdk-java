package org.eclipse.dataplane;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.eclipse.dataplane.domain.dataflow.DataFlowPrepareMessage;
import org.eclipse.dataplane.domain.dataflow.DataFlowStartMessage;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Objects;
import java.util.Random;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.restassured.RestAssured.given;

public class DataplaneClient {

    private final String baseUri;

    public DataplaneClient(String baseUri) {
        this.baseUri = baseUri;
    }

    public ValidatableResponse prepare(DataFlowPrepareMessage prepareMessage) {
        return given()
                .contentType(ContentType.JSON)
                .baseUri(baseUri)
                .body(prepareMessage)
                .post("/v1/dataflows/prepare")
                .then()
                .log().ifValidationFails();
    }

    public ValidatableResponse start(DataFlowStartMessage startMessage) {
        return given()
                .contentType(ContentType.JSON)
                .baseUri(baseUri)
                .body(startMessage)
                .post("/v1/dataflows/start")
                .then()
                .log().ifValidationFails();
    }

    public ValidatableResponse status(String dataFlowId) {
        return given()
                .baseUri(baseUri)
                .get("/v1/dataflows/{id}/status", dataFlowId)
                .then()
                .log().ifValidationFails();
    }

    private int getFreePort() {
        var port = new Random().nextInt(16384, 65536);
        try (var serverSocket = new ServerSocket(port)) {
            Objects.requireNonNull(serverSocket);
            return port;
        } catch (IOException e) {
            return getFreePort();
        }
    }

    public ValidatableResponse completed(String dataFlowId) {
        return given()
                .baseUri(baseUri)
                .post("/v1/dataflows/{id}/completed", dataFlowId)
                .then()
                .log().ifValidationFails();
    }
}
