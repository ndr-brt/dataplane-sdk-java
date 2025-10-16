package org.eclipse.dataplane;

import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.eclipse.dataplane.domain.dataflow.DataFlowPrepareMessage;

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

    public ValidatableResponse status(String dataFlowId) {
        return given()
                .baseUri(baseUri)
                .get("/v1/dataflows/{id}/status", dataFlowId)
                .then()
                .log().ifValidationFails();
    }
}
