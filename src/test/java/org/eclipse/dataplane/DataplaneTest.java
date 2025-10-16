package org.eclipse.dataplane;

import io.restassured.http.ContentType;
import org.eclipse.dataplane.domain.DataAddress;
import org.eclipse.dataplane.domain.Result;
import org.eclipse.dataplane.domain.dataflow.DataFlowPrepareMessage;
import org.eclipse.dataplane.domain.dataflow.DataFlowResponseMessage;
import org.eclipse.dataplane.domain.dataflow.DataFlowStatusResponseMessage;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.ee10.servlet.Source;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.jetty.ee10.servlet.ServletContextHandler.NO_SESSIONS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DataplaneTest {

    private final int port = 8090;
    private final HttpServer httpServer = new HttpServer(port);
    private final OnPrepare onPrepare = mock();
    private final DataplaneClient client = new DataplaneClient("http://localhost:" + port);

    @BeforeEach
    void setUp() {
        httpServer.start();
        var dataplane = Dataplane.newInstance()
                .id("thisDataplaneId")
                .onPrepare(onPrepare)
                .build();

        httpServer.deploy(dataplane.controller());
    }

    @AfterEach
    void tearDown() {
        httpServer.stop();
    }

    @Nested
    class Prepare {
        @Test
        void shouldBePrepared_whenDataAddressIsDefined() {
            when(onPrepare.action(any())).thenReturn(Result.success(new DataAddress("HttpData", "Http", "http://endpoint.somewhere", emptyList())));
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
        void shouldBePreparing_whenDataAddressIsNull() {
            when(onPrepare.action(any())).thenReturn(Result.success(null));

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

    private static class HttpServer {

        private final Server server;
        private final ServletContextHandler servletContextHandler = new ServletContextHandler(NO_SESSIONS);

        public HttpServer(int port) {
            server = new Server();
            var connector = new ServerConnector(server);
            connector.setPort(port);
            server.setConnectors(new Connector[] {connector});
            server.setHandler(servletContextHandler);
        }

        public void start() {
            try {
                server.start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public void deploy(Object controller) {
            var servletHolder = createServletHolder(controller);
            servletContextHandler.getServletHandler().addServletWithMapping(servletHolder, "/*");
        }

        public void stop() {
            try {
                server.stop();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private ServletHolder createServletHolder(Object controller) {
            var resourceConfig = new ResourceConfig();
            resourceConfig.registerClasses(controller.getClass());
            resourceConfig.registerInstances(new AbstractBinder() {
                @Override
                protected void configure() {
                    bind(controller).to((Class<? super Object>) controller.getClass());
                }
            });
            var servlet = new ServletContainer(resourceConfig);
            var servletHolder = new ServletHolder(Source.EMBEDDED);
            servletHolder.setServlet(servlet);
            return servletHolder;
        }
    }
}
