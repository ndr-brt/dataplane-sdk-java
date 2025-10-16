package org.eclipse.dataplane.domain.dataflow;

import org.eclipse.dataplane.domain.DataAddress;

public record DataFlowResponseMessage(
        String dataplaneId,
        DataAddress dataAddress,
        String state,
        String error
) {
}
