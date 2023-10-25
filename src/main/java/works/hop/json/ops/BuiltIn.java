package works.hop.json.ops;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum BuiltIn {

    ABS("abs"), AVG("avg"), CONTAINS("contains"), CEIL("ceil"), ENDS_WITH("ends_with"),
    FLOOR("floor"), JOIN("join"), KEYS("keys"), LENGTH("length"), MAP("map"), MAX("max"),
    MAX_BY("max_by");

    public final String symbol;

    BuiltIn(String symbol) {
        this.symbol = symbol;
    }

    public static List<String> functions(){
        return Arrays.stream(values()).map(v -> v.symbol).collect(Collectors.toList());
    }

    public static BuiltIn of(String function) {
        for(BuiltIn type : values()){
            if(type.symbol.equals(function)){
                return type;
            }
        }
        throw new RuntimeException(String.format("Unexpected function name - %s", function));
    }
}
