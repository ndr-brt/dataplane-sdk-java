package org.eclipse.dataplane.port.store;

import org.eclipse.dataplane.domain.dataflow.DataFlow;
import org.eclipse.dataplane.domain.Result;

public interface DataFlowStore {
    Result<Void> save(DataFlow dataFlow);

    Result<DataFlow> findById(String flowId);
}
