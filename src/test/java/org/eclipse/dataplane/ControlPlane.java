package org.eclipse.dataplane;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.response.ValidatableResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.eclipse.dataplane.domain.dataflow.DataFlowPrepareMessage;
import org.eclipse.dataplane.domain.dataflow.DataFlowStartMessage;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * This simulates control plane for both consumer and provider
 */
public class ControlPlane {

    private final DataplaneClient consumerClient;
    private final DataplaneClient providerClient;
    private final HttpServer httpServer;

    public ControlPlane(HttpServer httpServer, String consumerUrl, String providerUrl) {
        this.httpServer = httpServer;
        httpServer.deploy("/consumer/control-plane", new ControlPlaneController());
        httpServer.deploy("/provider/control-plane", new ControlPlaneController());

        consumerClient = new DataplaneClient(consumerUrl);
        providerClient = new DataplaneClient(providerUrl);
    }

    public ValidatableResponse consumerPrepare(DataFlowPrepareMessage prepareMessage) {
        return consumerClient.prepare(prepareMessage);
    }

    public ValidatableResponse providerStart(DataFlowStartMessage startMessage) {
        return providerClient.start(startMessage);
    }

    public ValidatableResponse providerStatus(String flowId) {
        return providerClient.status(flowId);
    }

    public String providerCallbackAddress() {
        return "http://localhost:%d/provider/control-plane".formatted(httpServer.port());
    }

    @Path("/transfers")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    public static class ControlPlaneController {

        @POST
        @Path("/{transferId}/dataflow/completed")
        public void completed() {
            // notify consumer
        }

    }
}
