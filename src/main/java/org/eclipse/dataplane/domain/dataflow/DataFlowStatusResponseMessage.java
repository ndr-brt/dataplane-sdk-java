package org.eclipse.dataplane.domain.dataflow;

public record DataFlowStatusResponseMessage(
        String dataflowId,
        String state
) {
}
