package org.eclipse.dataplane.port;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import org.eclipse.dataplane.Dataplane;
import org.eclipse.dataplane.domain.DataFlowPrepareMessage;
import org.eclipse.dataplane.domain.DataFlowResponseMessage;
import org.eclipse.dataplane.domain.DataFlowStatusResponseMessage;

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
        return dataplane.prepare(message).getOrElseThrow(() -> new RuntimeException("cannot prepare")); // TODO: pass reason!
    }

    @GET
    @Path("/{flowId}/status")
    public DataFlowStatusResponseMessage status(@PathParam("flowId") String flowId) {
        return dataplane.findById(flowId)
                .map(f -> new DataFlowStatusResponseMessage(f.getId(), f.getState().name()))
                .getOrElseThrow(() -> new RuntimeException("cannot execute action")); // TODO: manage reason!
    }

}
