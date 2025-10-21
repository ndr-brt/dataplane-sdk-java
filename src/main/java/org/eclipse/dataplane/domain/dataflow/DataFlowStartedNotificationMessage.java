package org.eclipse.dataplane.domain.dataflow;

import org.eclipse.dataplane.domain.DataAddress;

public record DataFlowStartedNotificationMessage(
        DataAddress dataAddress
) {
}
