package org.eclipse.dataplane;

import org.eclipse.dataplane.domain.dataflow.DataFlowPrepareMessage;
import org.eclipse.dataplane.domain.Result;

public interface OnPrepare {

    Result<Void> action(DataFlowPrepareMessage dataFlowPrepareMessage);

}
