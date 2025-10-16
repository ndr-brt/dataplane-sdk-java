package org.eclipse.dataplane;

import org.eclipse.dataplane.domain.DataAddress;
import org.eclipse.dataplane.domain.Result;
import org.eclipse.dataplane.domain.dataflow.DataFlowPrepareMessage;

public interface OnPrepare {

    Result<DataAddress> action(DataFlowPrepareMessage dataFlowPrepareMessage);

}
