package org.eclipse.dataplane;

import org.eclipse.dataplane.domain.Prepare;
import org.eclipse.dataplane.domain.Result;

public interface OnPrepare {

    Result<Object, Object> action(Prepare prepare);

}
