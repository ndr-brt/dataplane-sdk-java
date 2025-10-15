package org.eclipse.dataplane;

import org.eclipse.dataplane.domain.DataFlow;
import org.eclipse.dataplane.domain.DataFlowPrepareMessage;
import org.eclipse.dataplane.domain.DataFlowResponseMessage;
import org.eclipse.dataplane.domain.DataFlowStatusResponseMessage;
import org.eclipse.dataplane.domain.Result;
import org.eclipse.dataplane.port.DataPlaneSignalingApiController;
import org.eclipse.dataplane.port.store.DataFlowStore;
import org.eclipse.dataplane.port.store.InMemoryDataFlowStore;

public class Dataplane {

    private final DataFlowStore store = new InMemoryDataFlowStore();
    private OnPrepare onPrepare;

    public static Builder newInstance() {
        return new Builder();
    }

    public DataPlaneSignalingApiController controller() {
        return new DataPlaneSignalingApiController(this);
    }

    public Result<DataFlowResponseMessage> prepare(DataFlowPrepareMessage message) {
        var dataFlow = DataFlow.newInstance().id(message.processId()).state(DataFlow.State.PREPARING).build();
        var response = onPrepare.action(message).orElseThrow(e -> new RuntimeException("Cannot execute action", e));

        dataFlow.transitionToPrepared();

        return store.save(dataFlow).map(it -> response);
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
            return dataplane;
        }

        public Builder onPrepare(OnPrepare onPrepare) {
            dataplane.onPrepare = onPrepare;
            return this;
        }
    }
}
