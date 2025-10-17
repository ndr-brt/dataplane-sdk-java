package org.eclipse.dataplane.logic;

import org.eclipse.dataplane.domain.Result;
import org.eclipse.dataplane.domain.dataflow.DataFlow;

public interface OnStart {

    Result<DataFlow> action(DataFlow dataFlow);

}
