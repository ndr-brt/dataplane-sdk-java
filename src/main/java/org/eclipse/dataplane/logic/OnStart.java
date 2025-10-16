package org.eclipse.dataplane.logic;

import org.eclipse.dataplane.domain.DataAddress;
import org.eclipse.dataplane.domain.Result;
import org.eclipse.dataplane.domain.dataflow.DataFlowStartMessage;

import java.util.concurrent.CompletableFuture;

public interface OnStart {

    Result<CompletableFuture<DataAddress>> action(DataFlowStartMessage dataFlowStartMessage);

}
