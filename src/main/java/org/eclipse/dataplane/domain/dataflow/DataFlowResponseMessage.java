package org.eclipse.dataplane.domain.dataflow;

import java.util.Map;

public record DataFlowResponseMessage(
        String dataplaneId,
        Map<String, Object> dataAddress,
        String state,
        String error
) {
}
