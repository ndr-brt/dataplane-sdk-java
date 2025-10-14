package org.eclipse.dataplane;

import org.eclipse.dataplane.port.DataPlaneSignalingApiController;

public class Dataplane {

    private OnPrepare onPrepare;

    public static Builder newInstance() {
        return new Builder();
    }

    public DataPlaneSignalingApiController controller() {
        return new DataPlaneSignalingApiController(this);
    }

    public OnPrepare onPrepare() {
        return onPrepare;
    }

    public static class Builder {
        private final Dataplane dataplane = new Dataplane();

        private Builder() {
        }

        public Dataplane build() {
            return dataplane;
        }

        public Builder onPrepare(OnPrepare onPrepare) {
            dataplane.onPrepare = onPrepare;
            return this;
        }
    }
}
