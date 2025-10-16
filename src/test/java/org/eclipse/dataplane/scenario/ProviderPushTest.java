package org.eclipse.dataplane.scenario;

import org.eclipse.dataplane.Dataplane;
import org.eclipse.dataplane.domain.Result;
import org.junit.jupiter.api.Test;

public class ProviderPushTest {

    private final Dataplane consumer = Dataplane.newInstance()
            .id("thisDataplaneId")
            .onPrepare(message -> Result.success(null))
            .build();

    private final Dataplane provider = Dataplane.newInstance()
            .id("thisDataplaneId")
            .build();

    @Test
    void shouldPushDataToEndpointPreparedByConsumer() {

    }
}
