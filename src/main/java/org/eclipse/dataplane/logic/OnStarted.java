package org.eclipse.dataplane.logic;

import org.eclipse.dataplane.domain.Result;
import org.eclipse.dataplane.domain.dataflow.DataFlow;

public interface OnStarted {

    Result<DataFlow> action(DataFlow dataFlow);

}
