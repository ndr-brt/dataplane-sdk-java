package org.eclipse.dataplane;

import org.eclipse.dataplane.domain.dataflow.DataFlow;
import org.eclipse.dataplane.domain.dataflow.DataFlowPrepareMessage;
import org.eclipse.dataplane.domain.dataflow.DataFlowResponseMessage;
import org.eclipse.dataplane.domain.dataflow.DataFlowStatusResponseMessage;
import org.eclipse.dataplane.domain.Result;
import org.eclipse.dataplane.port.DataPlaneSignalingApiController;
import org.eclipse.dataplane.port.store.DataFlowStore;
import org.eclipse.dataplane.port.store.InMemoryDataFlowStore;

import java.util.Collections;
import java.util.UUID;

public class Dataplane {

    private final DataFlowStore store = new InMemoryDataFlowStore();
    private String id;
    private OnPrepare onPrepare;

    public static Builder newInstance() {
        return new Builder();
    }

    public DataPlaneSignalingApiController controller() {
        return new DataPlaneSignalingApiController(this);
    }

    public Result<DataFlowResponseMessage> prepare(DataFlowPrepareMessage message) {
        var dataFlow = DataFlow.newInstance().id(message.processId()).state(DataFlow.State.PREPARING).build();
        return Result.attempt(() -> onPrepare.action(message))
                        .flatMap(r -> {
                            dataFlow.transitionToPrepared();
                            var response = new DataFlowResponseMessage(id, Collections.emptyMap(), dataFlow.getState().name(), "");
                            return store.save(dataFlow).map(it -> response);
                        });
    }

    public Result<DataFlowStatusResponseMessage> status(String dataFlowId) {
        return store.findById(dataFlowId)
                .map(f -> new DataFlowStatusResponseMessage(f.getId(), f.getState().name())); // TODO: manage reason!
    }

    public Result<DataFlow> findById(String flowId) {
        return store.findById(flowId);
    }

    public static class Builder {
        private final Dataplane dataplane = new Dataplane();

        private Builder() {
        }

        public Dataplane build() {
            if (dataplane.id == null) {
                dataplane.id = UUID.randomUUID().toString();
            }
            return dataplane;
        }

        public Builder onPrepare(OnPrepare onPrepare) {
            dataplane.onPrepare = onPrepare;
            return this;
        }

        public Builder id(String id) {
            dataplane.id = id;
            return this;
        }
    }
}
