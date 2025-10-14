package org.eclipse.dataplane.domain;

import java.util.Map;

public record DataFlowPrepareMessage(
        String messageId,
        String participantId,
        String counterPartyId,
        String dataspaceContext,
        String processId,
        String agreementId,
        String datasetId,
        String callbackAddress,
        String transferType,
        Map<String, Object> dataAddress
) {
}
