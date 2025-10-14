package org.eclipse.dataplane.domain;

import java.util.function.Supplier;

public abstract class Result<C> {

    public static <C> Result<C> success(C content) {
        return new Success<>(content);
    }

    public abstract <X extends Throwable> C getOrElseThrow(Supplier<X> exceptionSupplier) throws X;

    private static class Success<C> extends Result<C> {

        private final C content;

        public Success(C content) {
            this.content = content;
        }

        @Override
        public <X extends Throwable> C getOrElseThrow(Supplier<X> exceptionSupplier) throws X {
            return content;
        }
    }

    private static class Failure<C> extends Result<C> {

        @Override
        public <X extends Throwable> C getOrElseThrow(Supplier<X> exceptionSupplier) throws X {
            throw exceptionSupplier.get();
        }
    }
}
