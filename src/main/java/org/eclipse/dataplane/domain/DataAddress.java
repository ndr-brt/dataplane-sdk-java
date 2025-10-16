package org.eclipse.dataplane.domain;

import java.util.List;

public record DataAddress(
        String type,
        String endpointType,
        String endpoint,
        List<EndpointProperty> endpointProperties
) {

    public record EndpointProperty (
        String type,
        String name,
        String value
    ) {

    }
}
