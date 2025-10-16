package org.eclipse.dataplane;

import io.restassured.http.ContentType;
import org.eclipse.dataplane.domain.DataAddress;
import org.eclipse.dataplane.domain.Result;
import org.eclipse.dataplane.domain.dataflow.DataFlowPrepareMessage;
import org.eclipse.dataplane.domain.dataflow.DataFlowResponseMessage;
import org.eclipse.dataplane.domain.dataflow.DataFlowStatusResponseMessage;
import org.eclipse.dataplane.logic.OnPrepare;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DataplaneTest {

    private final int port = 8090;
    private final HttpServer httpServer = new HttpServer(port);
    private final OnPrepare onPrepare = mock();
    private final DataplaneClient client = new DataplaneClient("http://localhost:" + port + "/api");

    @BeforeEach
    void setUp() {
        httpServer.start();
        var dataplane = Dataplane.newInstance()
                .id("thisDataplaneId")
                .onPrepare(onPrepare)
                .build();

        httpServer.deploy("/api", dataplane.controller());
    }

    @AfterEach
    void tearDown() {
        httpServer.stop();
    }

    @Nested
    class Prepare {
        @Test
        void shouldBePrepared_whenOnPrepareCompleted() {
            when(onPrepare.action(any())).thenReturn(Result.success(CompletableFuture.completedFuture(new DataAddress("HttpData", "Http", "http://endpoint.somewhere", emptyList()))));
            var prepareMessage = createPrepareMessage("theProcessId");

            var prepareResponse = client.prepare(prepareMessage)
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .extract().as(DataFlowResponseMessage.class);

            assertThat(prepareResponse.dataplaneId()).isEqualTo("thisDataplaneId");
            assertThat(prepareResponse.dataAddress()).isNotNull();
            assertThat(prepareResponse.state()).isEqualTo("PREPARED");
            assertThat(prepareResponse.error()).isNull();

            var statusResponse = client.status("theProcessId")
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .extract().as(DataFlowStatusResponseMessage.class);

            assertThat(statusResponse.dataflowId()).isEqualTo("theProcessId");
            assertThat(statusResponse.state()).isEqualTo("PREPARED");
        }

        @Test
        void shouldBePreparing_whenOnPrepareNotCompletedYet() {
            when(onPrepare.action(any())).thenReturn(Result.success(new CompletableFuture<>()));

            var prepareMessage = createPrepareMessage("theProcessId");

            var prepareResponse = client.prepare(prepareMessage)
                    .statusCode(202)
                    .contentType(ContentType.JSON)
                    .extract().as(DataFlowResponseMessage.class);

            assertThat(prepareResponse.dataplaneId()).isEqualTo("thisDataplaneId");
            assertThat(prepareResponse.dataAddress()).isNull();
            assertThat(prepareResponse.state()).isEqualTo("PREPARING");
            assertThat(prepareResponse.error()).isNull();
        }

        private DataFlowPrepareMessage createPrepareMessage(String processId) {
            return new DataFlowPrepareMessage("theMessageId", "theParticipantId", "theCounterPartyId",
                    "theDatapaceContext", processId, "theAgreementId", "theDatasetId", "theCallbackAddress",
                    "theTransferType", null);
        }
    }

    @Nested
    class Status {

        @Test
        void shouldReturn404_whenDataFlowDoesNotExist() {
            client.status("unexistent").statusCode(404);
        }

    }

}
