package org.eclipse.dataplane;

import org.eclipse.dataplane.domain.DataFlowPrepareMessage;
import org.eclipse.dataplane.domain.DataFlowResponseMessage;
import org.eclipse.dataplane.domain.Result;

public interface OnPrepare {

    Result<DataFlowResponseMessage, Object> action(DataFlowPrepareMessage dataFlowPrepareMessage);

}
