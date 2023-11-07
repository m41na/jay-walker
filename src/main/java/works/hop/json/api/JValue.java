package works.hop.json.api;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class JValue implements JNode {

    Object value;

    @Override
    public boolean isArray() {
        return List.class.isAssignableFrom(value.getClass());
    }

    @Override
    public boolean isObject() {
        return Map.class.isAssignableFrom(value.getClass());
    }

    @Override
    public Object value() {
        return value;
    }

    @Override
    public <T> T value(Class<T> type) {
        if (value != null) {
            return type.cast(value);
        }
        return null;
    }

    @Override
    public <A, R> R value(Function<A, R> converter) {
        if (value != null) {
            return converter.apply((A) value);
        }
        return null;
    }
}
