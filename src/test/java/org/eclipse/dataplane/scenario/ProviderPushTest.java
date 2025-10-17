package org.eclipse.dataplane.scenario;

import org.eclipse.dataplane.ControlPlane;
import org.eclipse.dataplane.Dataplane;
import org.eclipse.dataplane.HttpServer;
import org.eclipse.dataplane.domain.DataAddress;
import org.eclipse.dataplane.domain.Result;
import org.eclipse.dataplane.domain.dataflow.DataFlow;
import org.eclipse.dataplane.domain.dataflow.DataFlowPrepareMessage;
import org.eclipse.dataplane.domain.dataflow.DataFlowResponseMessage;
import org.eclipse.dataplane.domain.dataflow.DataFlowStartMessage;
import org.eclipse.dataplane.domain.dataflow.DataFlowStatusResponseMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.Collections.emptyList;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.eclipse.dataplane.domain.dataflow.DataFlow.State.COMPLETED;

public class ProviderPushTest {

    private final int port = 21341;

    private final HttpServer httpServer = new HttpServer(21341);

    private final ControlPlane controlPlane = new ControlPlane(httpServer,
                "http://localhost:%d/consumer/data-plane".formatted(port),
                "http://localhost:%d/provider/data-plane".formatted(port));

    @BeforeEach
    void setUp() {
        httpServer.start();

        var consumer = Dataplane.newInstance()
                .id("thisDataplaneId")
                .onPrepare(message -> {
                    try {
                        var destinationFolder = Files.createTempDirectory("consumer-dest");
                        var dataAddress = new DataAddress("FileSystem", "folder", destinationFolder.toString(), emptyList());
                        return Result.success(completedFuture(dataAddress));
                    } catch (IOException e) {
                        return Result.failure(e);
                    }
                })
                .build();
        httpServer.deploy("/consumer/data-plane", consumer.controller());

        var providerDataPlane = new ProviderDataPlane(httpServer);
        providerDataPlane.start();
    }

    @AfterEach
    void tearDown() {
        httpServer.stop();
    }

    @Test
    void shouldPushDataToEndpointPreparedByConsumer() {
        var transferType = "FileSystem-PUSH";
        var consumerProcessId = UUID.randomUUID().toString();
        var prepareMessage = new DataFlowPrepareMessage("theMessageId", "theParticipantId", "theCounterPartyId",
                "theDataspaceContext", consumerProcessId, "theAgreementId", "theDatasetId", "theCallbackAddress",
                transferType, null);

        var prepareResponse = controlPlane.consumerPrepare(prepareMessage).statusCode(200).extract().as(DataFlowResponseMessage.class);
        assertThat(prepareResponse.state()).isEqualTo("PREPARED");
        assertThat(prepareResponse.dataAddress()).isNotNull();
        var destinationDataAddress = prepareResponse.dataAddress();

        var providerProcessId = UUID.randomUUID().toString();
        var startMessage = new DataFlowStartMessage("theMessageId", "theParticipantId", "theCounterPartyId",
                "theDataspaceContext", providerProcessId, "theAgreementId", "theDatasetId", controlPlane.providerCallbackAddress(),
                transferType, destinationDataAddress);
        var startResponse = controlPlane.providerStart(startMessage).statusCode(200).extract().as(DataFlowResponseMessage.class);

        assertThat(startResponse.state()).isEqualTo("STARTED");
        assertThat(startResponse.dataAddress()).isNull();

        await().untilAsserted(() -> {
            var path = Path.of(destinationDataAddress.endpoint()).resolve("data.txt");
            assertThat(path).exists().content().isEqualTo("hello world");
            var providerStatus = controlPlane.providerStatus(providerProcessId).statusCode(200).extract().as(DataFlowStatusResponseMessage.class);
            assertThat(providerStatus.state()).isEqualTo(COMPLETED.name());
        });
    }

    private static class ProviderDataPlane {

        private final HttpServer httpServer;
        private final ExecutorService executor = Executors.newCachedThreadPool();
        private final Dataplane sdk = Dataplane.newInstance()
                .id("provider")
                .onStart(this::onStart)
                .build();

        public ProviderDataPlane(HttpServer httpServer) {
            this.httpServer = httpServer;
        }

        public void start() {
            httpServer.deploy("/provider/data-plane", sdk.controller());
        }

        private Result<DataFlow> onStart(DataFlow dataFlow) {
            var dataAddress = dataFlow.getDataAddress();
            var future = CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(1000L); // simulates async transfer
                    var destination = Path.of(dataAddress.endpoint()).resolve("data.txt");
                    Files.writeString(destination, "hello world");
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }, executor);

            future.whenComplete((_v, throwable) -> {
                if (throwable == null) {
                    sdk.notifyCompleted(dataFlow);
                } else {
                    sdk.notifyErrored(dataFlow, throwable);
                }
            });

            dataFlow.transitionToStarted();
            return Result.success(dataFlow);
        }
    }
}
