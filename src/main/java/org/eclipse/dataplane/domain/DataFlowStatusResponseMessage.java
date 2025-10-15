package org.eclipse.dataplane.domain;

public record DataFlowStatusResponseMessage(
        String dataflowId,
        String state
) {
}
