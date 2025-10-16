package org.eclipse.dataplane.scenario;

import org.eclipse.dataplane.Dataplane;
import org.eclipse.dataplane.DataplaneClient;
import org.eclipse.dataplane.HttpServer;
import org.eclipse.dataplane.domain.DataAddress;
import org.eclipse.dataplane.domain.Result;
import org.eclipse.dataplane.domain.dataflow.DataFlowPrepareMessage;
import org.eclipse.dataplane.domain.dataflow.DataFlowResponseMessage;
import org.eclipse.dataplane.domain.dataflow.DataFlowStartMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class ProviderPushTest {

    private final DataplaneClient consumerClient = new DataplaneClient("http://localhost:21341/consumer");
    private final DataplaneClient providerClient = new DataplaneClient("http://localhost:21341/provider");

    private final HttpServer httpServer = new HttpServer(21341);

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
        httpServer.deploy("/consumer", consumer.controller());

        var provider = Dataplane.newInstance()
                .id("thisDataplaneId")
                .onStart(message -> {
                    var destination = Path.of(message.dataAddress().endpoint()).resolve("data.txt");
                    try {
                        Files.writeString(destination, "hello world");
                        return Result.success(completedFuture(null));
                    } catch (IOException e) {
                        return Result.failure(e);
                    }
                })
                .build();
        httpServer.deploy("/provider", provider.controller());
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

        var prepareResponse = consumerClient.prepare(prepareMessage).statusCode(200).extract().as(DataFlowResponseMessage.class);
        assertThat(prepareResponse.state()).isEqualTo("PREPARED");
        assertThat(prepareResponse.dataAddress()).isNotNull();
        var destinationDataAddress = prepareResponse.dataAddress();

        var startMessage = new DataFlowStartMessage("theMessageId", "theParticipantId", "theCounterPartyId",
                "theDataspaceContext", UUID.randomUUID().toString(), "theAgreementId", "theDatasetId", "theCallbackAddress",
                transferType, destinationDataAddress);
        var startResponse = providerClient.start(startMessage).statusCode(200).extract().as(DataFlowResponseMessage.class);

        assertThat(startResponse.state()).isEqualTo("STARTED");
        assertThat(startResponse.dataAddress()).isNull();

        await().untilAsserted(() -> {
            var content = Files.readString(Path.of(destinationDataAddress.endpoint()).resolve("data.txt"));
            assertThat(content).isEqualTo("hello world");
        });
    }
}
