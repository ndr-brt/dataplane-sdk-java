package org.eclipse.dataplane.logic;

import org.eclipse.dataplane.domain.DataAddress;
import org.eclipse.dataplane.domain.Result;
import org.eclipse.dataplane.domain.dataflow.DataFlowPrepareMessage;

import java.util.concurrent.CompletableFuture;

public interface OnPrepare {

    Result<CompletableFuture<DataAddress>> action(DataFlowPrepareMessage dataFlowPrepareMessage);

}
