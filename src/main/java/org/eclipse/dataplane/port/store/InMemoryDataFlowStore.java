package org.eclipse.dataplane.port.store;

import org.eclipse.dataplane.domain.DataFlow;
import org.eclipse.dataplane.domain.Result;

import java.util.HashMap;
import java.util.Map;

public class InMemoryDataFlowStore implements DataFlowStore {

    private final Map<String, DataFlow> store = new HashMap<>();

    @Override
    public Result<Void> save(DataFlow dataFlow) {
        store.put(dataFlow.getId(), dataFlow);
        return Result.success(null); // TODO: success withouth parameter
    }

    @Override
    public Result<DataFlow> findById(String flowId) {
        var dataFlow = store.get(flowId);
        if (dataFlow == null) {
            return Result.failure(new RuntimeException("Not found")); // TODO: SDK store exceptions
        }
        return Result.success(dataFlow);
    }
}
