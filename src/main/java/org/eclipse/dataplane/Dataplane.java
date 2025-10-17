package org.eclipse.dataplane;

import org.eclipse.dataplane.domain.Result;
import org.eclipse.dataplane.domain.dataflow.DataFlow;
import org.eclipse.dataplane.domain.dataflow.DataFlowPrepareMessage;
import org.eclipse.dataplane.domain.dataflow.DataFlowResponseMessage;
import org.eclipse.dataplane.domain.dataflow.DataFlowStartMessage;
import org.eclipse.dataplane.domain.dataflow.DataFlowStatusResponseMessage;
import org.eclipse.dataplane.logic.OnPrepare;
import org.eclipse.dataplane.logic.OnStart;
import org.eclipse.dataplane.port.DataPlaneSignalingApiController;
import org.eclipse.dataplane.port.store.DataFlowStore;
import org.eclipse.dataplane.port.store.InMemoryDataFlowStore;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

public class Dataplane {

    private final DataFlowStore store = new InMemoryDataFlowStore();
    private String id;
    private OnPrepare onPrepare = _m -> Result.failure(new UnsupportedOperationException("onPrepare is not implemented"));
    private OnStart onStart = _m -> Result.failure(new UnsupportedOperationException("onStarted is not implemented"));

    public static Builder newInstance() {
        return new Builder();
    }

    public DataPlaneSignalingApiController controller() {
        return new DataPlaneSignalingApiController(this);
    }

    public Result<DataFlowResponseMessage> prepare(DataFlowPrepareMessage message) {
        var dataFlow = DataFlow.newInstance()
                .id(message.processId())
                .state(DataFlow.State.PREPARING)
                .dataAddress(message.dataAddress())
                .callbackAddress(message.callbackAddress())
                .transferType(message.transferType())
                .build();

        return onPrepare.action(message)
                .compose(futureDataAddress -> {
                    DataFlowResponseMessage response;
                    if (futureDataAddress.isDone()) {
                        dataFlow.transitionToPrepared();
                        var dataAddress = futureDataAddress.join(); // TODO: manage the async case
                        response = new DataFlowResponseMessage(id, dataAddress, dataFlow.getState().name(), null);
                    } else {
                        dataFlow.transitionToPreparing();
                        response = new DataFlowResponseMessage(id, null, dataFlow.getState().name(), null);
                    }

                    return store.save(dataFlow).map(it -> response);
                });
    }


    public Result<DataFlowResponseMessage> start(DataFlowStartMessage message) {
        var initialDataFlow = DataFlow.newInstance()
                .id(message.processId())
                .state(DataFlow.State.STARTING)
                .dataAddress(message.dataAddress())
                .callbackAddress(message.callbackAddress())
                .transferType(message.transferType())
                .build();

        return onStart.action(initialDataFlow)
                .compose(dataFlow -> {
                    DataFlowResponseMessage response;
                    if (dataFlow.isStarted() && dataFlow.isPull()) {
                        response = new DataFlowResponseMessage(id, dataFlow.getDataAddress(), dataFlow.getState().name(), null);
                    } else {
                        response = new DataFlowResponseMessage(id, null, dataFlow.getState().name(), null);
                    }
                    return store.save(dataFlow).map(it -> response);
                });
    }

    public Result<DataFlowStatusResponseMessage> status(String dataFlowId) {
        return store.findById(dataFlowId)
                .map(f -> new DataFlowStatusResponseMessage(f.getId(), f.getState().name()));
    }

    /**
     * Notify the control plane that the data flow has been completed
     *
     * @param dataFlowId
     */
    public void notifyCompleted(String dataFlowId) {
            store.findById(dataFlowId)
                    .map(dataFlow -> {
                        try {
                            var endpoint = dataFlow.getCallbackAddress() + "/transfers/" + dataFlow.getId() + "/dataflow/completed";

                            var request = HttpRequest.newBuilder()
                                    .uri(URI.create(endpoint))
                                    .header("content-type", "application/json")
                                    .POST(HttpRequest.BodyPublishers.ofString("{}")) // TODO DataFlowCompletedMessage not defined
                                    .build();

                            var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.discarding());

                            // TODO: handle failure
                            // TODO: should this be done asynchronously? retry, and so on...

                            dataFlow.transitionToCompleted();
                            store.save(dataFlow);

                            // TODO: handle response
                            return null;
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                    });
    }

    /**
     * Notify the control plane that the data flow failed for some reason
     *
     * @param dataFlow
     * @param throwable
     */
    public void notifyErrored(String dataFlow, Throwable throwable) {
        // TODO: implementation
    }

    /**
     * Received notification that the flow has been completed
     *
     * @param flowId
     * @return
     */
    public Result<Void> completed(String flowId) {
        return store.findById(flowId).compose(dataFlow -> {
            dataFlow.transitionToCompleted();
            return store.save(dataFlow);
        });
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

        public Builder onStart(OnStart onStart) {
            dataplane.onStart = onStart;
            return this;
        }

        public Builder id(String id) {
            dataplane.id = id;
            return this;
        }
    }
}
