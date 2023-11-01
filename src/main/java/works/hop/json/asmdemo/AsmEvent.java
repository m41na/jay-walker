package works.hop.json.asmdemo;

import java.util.function.Function;

public interface AsmEvent {

    boolean isClass();

    boolean isField();

    boolean idMethod();

    boolean is(Class<?> type);

    Object value();

    <T>T value(Class<T> type);

    <V, T> T value(Function<V, T> converter);

    class StartMethod {
        String methodName;
    }
}
