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

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/v1/dataflows")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class DataPlaneSignalingApiController {

    private final Dataplane dataplane;

    public DataPlaneSignalingApiController(Dataplane dataplane) {
        this.dataplane = dataplane;
    }

    @GET
    @Path("/{flowId}/status")
    public Object status(@PathParam("flowId") String flowId) {
        return "bau";
    }

    @POST
    @Path("/prepare")
    public DataFlowResponseMessage prepare(DataFlowPrepareMessage message) {
        return dataplane.onPrepare().action(message).getOrElseThrow(() -> new RuntimeException("Cannot execute action")); // TODO: pass reason!
    }

}
