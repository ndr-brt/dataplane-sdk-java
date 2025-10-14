package org.eclipse.dataplane;

import io.restassured.http.ContentType;
import org.eclipse.dataplane.domain.Result;
import org.eclipse.dataplane.port.DataPlaneSignalingApiController;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

public class DataplaneTest {

    @Test
    void shouldPrepareTransfer() {
        var dataplane = Dataplane.newInstance().onPrepare(prepare -> {
            // do stuff
            return new Result<>();
        }).build();
        var port = 8090;

        var httpServer = new HttpServer(port);
        httpServer.start(DataPlaneSignalingApiController.class);

        given()
                .contentType(ContentType.JSON)
                .port(port)
                .post("/v1/dataflows/prepare")
                .then()
                .statusCode(200);

        httpServer.stop();
    }

    private static class HttpServer {

        private final Server server;

        public HttpServer(int port) {
            server = new Server();
            var connector = new ServerConnector(server);
            connector.setPort(port);
            server.setConnectors(new Connector[] {connector});
        }

        public void start(Class<?> controller) {
            var context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            var jerseyServlet = context.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/*");
            jerseyServlet.setInitParameter("jersey.config.server.provider.classnames", controller.getCanonicalName());

            server.setHandler(context);
            try {
                server.start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public void stop() {
            try {
                server.stop();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
