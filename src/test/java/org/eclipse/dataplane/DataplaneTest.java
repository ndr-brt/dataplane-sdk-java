package org.eclipse.dataplane;

import io.restassured.http.ContentType;
import org.eclipse.dataplane.domain.DataFlowResponseMessage;
import org.eclipse.dataplane.domain.Result;
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

import java.util.Collections;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.eclipse.jetty.ee10.servlet.ServletContextHandler.NO_SESSIONS;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;

public class DataplaneTest {

    private final int port = 8090;
    private final HttpServer httpServer = new HttpServer(port);

    @BeforeEach
    void setUp() {
        httpServer.start();
        var dataplane = Dataplane.newInstance()
                .onPrepare(prepare -> {
                    // do stuff
                    var response = new DataFlowResponseMessage("thisDataplaneId", Collections.emptyMap(), "STATE", "");
                    return Result.success(response);
                }).build();

        httpServer.deploy(dataplane.controller());
    }

    @AfterEach
    void tearDown() {
        httpServer.stop();
    }

    @Test
    void shouldPrepareTransfer() {
        given()
                .contentType(ContentType.JSON)
                .port(port)
                .body(Map.ofEntries(
                        Map.entry("processId", "theProcessId"),
                        Map.entry("messageId", "theMessageId"),
                        Map.entry("participantId", "theParticipantId")
                ))
                .post("/v1/dataflows/prepare")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("dataplaneId", is("thisDataplaneId"))
                .body("state", is("STATE"))
                .body("error", emptyString());

        given()
                .port(port)
                .get("/v1/dataflows/{id}/status", "theProcessId")
                .then()
                .statusCode(200)
                .body("dataflowId", is("theProcessId"))
                .body("state", is("PREPARED"));
    }

    @Test
    void shouldReturn404_whenDataFlowDoesNotExist() {
        given()
                .port(port)
                .get("/v1/dataflows/{id}/status", "unexistent")
                .then()
                .statusCode(404);
    }

    @Nested
    class Status {

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
