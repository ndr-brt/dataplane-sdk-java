package org.eclipse.dataplane;

import java.security.cert.CertPathBuilder;

public class Dataplane {

    private OnPrepare onPrepare;

    public static Builder newInstance() {
        return new Builder();
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
