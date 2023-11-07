package works.hop.json.api;

import java.util.function.Function;

public interface JNode {

    boolean isArray();

    boolean isObject();

    Object value();

    default boolean is(Class<?> type) {
        return type == value().getClass();
    }

    <T> T value(Class<T> type);

    <V, T> T value(Function<V, T> converter);
}
