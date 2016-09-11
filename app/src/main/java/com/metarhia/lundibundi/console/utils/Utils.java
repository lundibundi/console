package com.metarhia.lundibundi.console.utils;

import android.os.Build;
import android.os.Handler;
import android.view.View;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by lundibundi on 7/25/16.
 */
public class Utils {

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    /**
     * Generate a value suitable for use in {@link android.view.View#setId(int)}.
     * This value will not collide with ID values generated at build time by aapt for R.id.
     *
     * @return a generated ID value
     */
    private static int generateViewId(AtomicInteger nextGeneratedId) {
        for (; ; ) {
            final int result = nextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (nextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }

    public static int generateViewId() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return View.generateViewId();
        } else {
            return generateViewId(sNextGeneratedId);
        }
    }

    public static abstract class RunnableWithParams<T> implements Runnable {
        private T parameters;

        public T getParameters() {
            return parameters;
        }

        public void setParameters(T parameters) {
            this.parameters = parameters;
        }
    }

    public static abstract class ResultRunnable<T> implements Runnable {
        private T result;

        public T getResult() {
            return result;
        }

        public void setResult(T result) {
            this.result = result;
        }
    }

    public static abstract class Function<T, R> {
        public abstract R apply(T param) throws Exception;

        public <V> Function<T, V> andThen(final Function<? super R, ? extends V> after) {
            return new Function<T, V>() {
                @Override
                public V apply(T param) throws Exception {
                    return after.apply(Function.this.apply(param));
                }
            };
        }
    }

    public static abstract class BiFunction<T1, T2, R> {
        public abstract R apply(T1 p1, T2 p2) throws Exception;

    }

    public static class SubCompletableFuture<T, R> extends RunnableWithParams<T> {
        private Handler mHandler;
        private SubCompletableFuture<R, ?> mNext;
        private SubCompletableFuture mFirst;
        private Object mFunc;

        public SubCompletableFuture(Handler handler, BiFunction<T, ? extends Throwable, R> func) {
            this(handler, (Object) func);
        }

        public SubCompletableFuture(Handler handler, Function<T, R> func) {
            this(handler, (Object) func);
        }

        public SubCompletableFuture(Handler handler, Callable<R> func) {
            this(handler, (Object) func);
        }

        public SubCompletableFuture(Handler handler) {
            this(handler, (Object) null);
        }

        private SubCompletableFuture(Handler handler, Object func) {
            mHandler = handler;
            mFunc = func;
            mFirst = this;
        }

        public void start() {
            mFirst.startThis();
        }

        public SubCompletableFuture getFirstFuture() {
            return mFirst;
        }

        public void startThis() {
            startThis(null);
        }

        public void startThis(final T input) {
            setParameters(input);
            run();
        }

        @Override
        public void run() {
            execute(null, getParameters(), null);
        }

        private void execute(Handler prevHandler, final T input, final Throwable t1) {
            if (prevHandler == mHandler) execute(input, t1);
            else mHandler.post(new Runnable() {
                @Override
                public void run() {
                    execute(input, t1);
                }
            });
        }

        private void execute(T input, Throwable t1) {
            if (mFunc == null
                || t1 != null
                && (mFunc instanceof Function || mFunc instanceof Callable)) return;


            R result = null;
            Throwable t2 = null;
            try {
                if (mFunc instanceof Callable) {
                    Callable<R> func = (Callable<R>) mFunc;
                    result = func.call();
                } else if (mFunc instanceof Function) {
                    Function<T, R> func = (Function<T, R>) mFunc;
                    result = func.apply(input);
                } else if (mFunc instanceof BiFunction) {
                    BiFunction<T, Throwable, R> func = (BiFunction<T, Throwable, R>) mFunc;
                    result = func.apply(input, t1);
                }
            } catch (Exception e) {
                t2 = e;
            }
            if (mNext != null) mNext.execute(mHandler, result, t2);
        }

        public <V> SubCompletableFuture<R, V> thenApply(Function<R, V> after) {
            return thenApply(mHandler, after);
        }

        public <V> SubCompletableFuture<R, V> thenApply(Handler handler, Function<R, V> after) {
            SubCompletableFuture<R, V> nextFuture = new SubCompletableFuture<>(handler, after);
            nextFuture.mFirst = mFirst;
            mNext = nextFuture;
            return nextFuture;
        }

        public <V> SubCompletableFuture<R, V> thenAccept(BiFunction<R, ? extends Throwable, V> after) {
            return thenAccept(mHandler, after);
        }

        public <V> SubCompletableFuture<R, V> thenAccept(Handler handler, BiFunction<R, ? extends Throwable, V> after) {
            SubCompletableFuture<R, V> nextFuture = new SubCompletableFuture<>(handler, after);
            nextFuture.mFirst = mFirst;
            mNext = nextFuture;
            return nextFuture;
        }

        public <V> SubCompletableFuture<R, V> thenAccept(Callable<V> after) {
            return thenAccept(mHandler, after);
        }

        public <V> SubCompletableFuture<R, V> thenAccept(Handler handler, Callable<V> after) {
            SubCompletableFuture<R, V> nextFuture = new SubCompletableFuture<>(handler, after);
            nextFuture.mFirst = mFirst;
            mNext = nextFuture;
            return nextFuture;
        }

        public void setFunc(Callable<R> func) {
            mFunc = func;
        }

        public void setFunc(Function<T, R> func) {
            mFunc = func;
        }

        public void setFunc(BiFunction<T, ? extends Throwable, R> func) {
            mFunc = func;
        }

    }
}
