package org.eclipse.dataplane.domain.dataflow;

import java.util.Objects;

public class DataFlow {
    private String id;
    private State state;
    private String transferType;

    public static DataFlow.Builder newInstance() {
        return new Builder();
    }

    public String getId() {
        return id;
    }

    public State getState() {
        return state;
    }

    public void transitionToPrepared() {
        state = State.PREPARED;
    }

    public void transitionToPreparing() {
        state = State.PREPARING;
    }

    public void transitionToStarting() {
        state = State.STARTING;
    }

    public void transitionToStarted() {
        state = State.STARTED;
    }

    public boolean isPush() {
        return transferType.split("-")[1].equalsIgnoreCase("PUSH");
    }

    public static class Builder {
        private final DataFlow dataFlow = new DataFlow();

        private Builder() {

        }

        public DataFlow build() {
            Objects.requireNonNull(dataFlow.id);

            if (dataFlow.state == null) {
                dataFlow.state = State.INITIATING;
            }

            return dataFlow;
        }

        public Builder id(String id) {
            dataFlow.id = id;
            return this;
        }

        public Builder state(State state) {
            dataFlow.state = state;
            return this;
        }

        public Builder transferType(String transferType) {
            dataFlow.transferType = transferType;
            return this;
        }
    }

    public enum State {
        INITIATING,
        PREPARING,
        PREPARED,
        STARTING,
        STARTED
    }
}
