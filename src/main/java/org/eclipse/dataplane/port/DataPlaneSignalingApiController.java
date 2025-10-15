package org.eclipse.dataplane.port;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.dataplane.Dataplane;
import org.eclipse.dataplane.domain.DataFlowPrepareMessage;
import org.eclipse.dataplane.domain.DataFlowResponseMessage;
import org.eclipse.dataplane.domain.DataFlowStatusResponseMessage;
import org.eclipse.dataplane.port.exception.DataFlowNotFoundException;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/v1/dataflows")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class DataPlaneSignalingApiController {

    private final Dataplane dataplane;

    public DataPlaneSignalingApiController(Dataplane dataplane) {
        this.dataplane = dataplane;
    }

    @POST
    @Path("/prepare")
    public DataFlowResponseMessage prepare(DataFlowPrepareMessage message) {
        return dataplane.prepare(message).orElseThrow(this::mapToWsRsException);
    }

    @GET
    @Path("/{flowId}/status")
    public DataFlowStatusResponseMessage status(@PathParam("flowId") String flowId) {
        return dataplane.status(flowId).orElseThrow(this::mapToWsRsException);
    }

    private WebApplicationException mapToWsRsException(Exception exception) {
        if (exception instanceof DataFlowNotFoundException notFound) {
            return new NotFoundException(notFound);
        }
        return new WebApplicationException("unexpected internal server error");
    }

}
