package org.eclipse.dataplane.port;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/v1/dataflows")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class DataPlaneSignalingApiController {

    @POST
    @Path("/prepare")
    public Object prepare() {
        return "";
    }

}
