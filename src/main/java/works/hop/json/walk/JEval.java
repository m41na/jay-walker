package works.hop.json.walk;

import works.hop.json.api.JArray;
import works.hop.json.api.JNode;
import works.hop.json.api.JObject;
import works.hop.json.api.JValue;
import works.hop.json.ops.BuiltIn;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class JEval {

    final List<Token> tokens;
    final boolean filterNull;
    int current = 0;

    public JEval(List<Token> tokens) {
        this(tokens, true);
    }

    public JEval(List<Token> tokens, boolean filterNull) {
        this.tokens = tokens;
        this.filterNull = filterNull;
    }

    public static boolean compareLessOrEqualTo(JNode value, String property, String literal) {
        BigDecimal expected = new BigDecimal(literal);
        BigDecimal actual = value.value(JObject.class).get(property).value(BigDecimal.class);
        return actual.compareTo(expected) <= 0;
    }

    public static boolean compareLessThan(JNode value, String property, String literal) {
        BigDecimal expected = new BigDecimal(literal);
        BigDecimal actual = value.value(JObject.class).get(property).value(BigDecimal.class);
        return actual.compareTo(expected) < 0;
    }

    public static boolean compareGreaterOrEqualTo(JNode value, String property, String literal) {
        BigDecimal expected = new BigDecimal(literal);
        BigDecimal actual = value.value(JObject.class).get(property).value(BigDecimal.class);
        return actual.compareTo(expected) >= 0;
    }

    public static boolean compareGreaterThan(JNode value, String property, String literal) {
        BigDecimal expected = new BigDecimal(literal);
        BigDecimal actual = value.value(JObject.class).get(property).value(BigDecimal.class);
        return actual.compareTo(expected) > 0;
    }

    public static boolean compareInequality(JNode value, String property, String literal) {
        if (value.value(JObject.class).get(property).value().getClass().equals(String.class)) {
            return !value.value(JObject.class).get(property).value(String.class).equals(literal);
        } else {
            BigDecimal expected = new BigDecimal(literal);
            BigDecimal actual = value.value(JObject.class).get(property).value(BigDecimal.class);
            return actual.compareTo(expected) != 0;
        }
    }

    public static boolean compareEquality(JNode value, String property, String literal) {
        if (value.value(JObject.class).get(property).value().getClass().equals(String.class)) {
            return value.value(JObject.class).get(property).value(String.class).equals(literal);
        } else {
            BigDecimal expected = new BigDecimal(literal);
            BigDecimal actual = value.value(JObject.class).get(property).value(BigDecimal.class);
            return actual.compareTo(expected) == 0;
        }
    }

    public JNode evaluate(JNode root) {
        if (current < tokens.size()) {
            Token token = tokens.get(current);
            switch (token.type) {
                case IDENTIFIER: {
                    JNode node = root.value(JObject.class).get(token.getValue().toString());
                    step();
                    return evaluate(node);
                }
                case FUNCTION: {
                    BuiltIn function = (BuiltIn) token.getValue();
                    return evaluate(builtInFunction(root, function));
                }
                case NUMBER: {
                    Token before = tokens.get(current - 1);
                    Token after = tokens.get(current + 1);

                    if (before.type == TokenType.START_IDX && after.type == TokenType.END_IDX) {
                        JNode node = root.value(JArray.class).get(Integer.parseInt(token.getValue().toString()));
                        step();
                        return evaluate(node);
                    } else {
                        step();
                        return evaluate(root);
                    }
                }
                case PERIOD: {
                    if (Objects.requireNonNull(peek()).type == TokenType.STAR) {
                        step();
                        return evaluate(root);
                    } else if (root.isArray()) {
                        expectToken(TokenType.IDENTIFIER);
                        token = tokens.get(current);
                        //MUST be a projection
                        String key = token.getValue().toString();
                        return projectionFilter(root, key, false);
                    } else if (root.isObject()) {
                        expectToken(TokenType.IDENTIFIER);
                        token = tokens.get(current);
                        //MUST be an object attributes
                        String key = token.getValue().toString();
                        JNode node = root.value(JObject.class).get(key);
                        step();
                        return evaluate(Objects.requireNonNull(node));
                    } else {
                        step();
                        return evaluate(root);
                    }
                }
                case START_IDX: {
                    step();
                    return evaluate(root);
                }
                case END_IDX: {
                    Token before = tokens.get(current - 1);

                    if (before.type.equals(TokenType.START_IDX)) {
                        expectToken(TokenType.PERIOD);
                        if(Objects.requireNonNull(peek()).type == TokenType.IDENTIFIER) {
                            // a case for projection
                            expectToken(TokenType.IDENTIFIER);
                            String key = tokens.get(current).getValue().toString();
                            return projectionFilter(root, key, true);
                        }
                        if(Objects.requireNonNull(peek()).type == TokenType.START_IDX) {
                            // a case for multiselect list -> creates and returns a list
                            expectToken(TokenType.START_IDX);
                            step();
                            JArray multiList = new JArray();
                            while(current < tokens.size() && tokens.get(current).type != TokenType.END_IDX) {
                                List<Token> selector = new LinkedList<>();
                                while (tokens.get(current).type != TokenType.COMMA &&
                                        tokens.get(current).type != TokenType.END_IDX) {
                                    selector.add(tokens.get(current));
                                    step();
                                }

                                JArray array = root.value(JArray.class).stream()
                                        .map(v -> new JEval(selector).evaluate(v))
                                        .collect(Collectors.toCollection(JArray::new));

                                for(int i = 0; i < root.value(JArray.class).size(); i++){
                                    if(multiList.size() <= i){
                                        multiList.add(new JValue(new JArray()));
                                    }
                                    multiList.get(i).value(JArray.class).add(array.get(i));
                                }

                                step();
                            }
                            return new JValue(multiList);
                        }
                        if(Objects.requireNonNull(peek()).type == TokenType.LEFT_CURLY) {
                            // a case for multiselect hash -> creates and returns a hash
                            expectToken(TokenType.LEFT_CURLY);
                            step();
                            JArray multiHash = new JArray();
                            while(current < tokens.size() && tokens.get(current).type != TokenType.RIGHT_CURLY) {
                                String identifier = tokens.get(current).getValue().toString();
                                expectToken(TokenType.COLON);
                                step();
                                List<Token> selector = new LinkedList<>();
                                while (tokens.get(current).type != TokenType.COMMA &&
                                        tokens.get(current).type != TokenType.RIGHT_CURLY) {
                                    selector.add(tokens.get(current));
                                    step();
                                }

                                JArray array = root.value(JArray.class).stream()
                                        .map(v -> {
                                            JNode value = new JEval(selector).evaluate(v);
                                            JObject obj = new JObject();
                                            obj.put(identifier, value);
                                            return new JValue(obj);
                                        })
                                        .collect(Collectors.toCollection(JArray::new));

                                for(int i = 0; i < root.value(JArray.class).size(); i++){
                                    if(multiHash.size() <= i){
                                        multiHash.add(array.get(i));
                                    }
                                    else {
                                        multiHash.get(i).value(JObject.class).putAll(array.get(i).value(JObject.class));
                                    }
                                }

                                step();
                            }
                            return new JValue(multiHash);
                        }
                        // other unidentified scenario
                        throw new RuntimeException(String.format("not-yet-identified scenario - token %s, value %s", token.type, token.value));
                    } else {
                        step();
                        return evaluate(root);
                    }
                }
                case COLON: {
                    int length = root.value(JArray.class).size();
                    Map<Integer, Integer> delimiters = new LinkedHashMap<>();
                    int key = 1;
                    Token currentToken = tokens.get(--current);
                    while (currentToken.type != TokenType.END_IDX) {
                        if (currentToken.type == TokenType.NUMBER) {
                            delimiters.put(key, Integer.parseInt(currentToken.getValue().toString()));
                        }
                        if (currentToken.type == TokenType.COLON) {
                            key += 1;
                        }
                        step();
                        currentToken = tokens.get(current);
                    }

                    int start = delimiters.getOrDefault(1, 0);
                    int stop = delimiters.getOrDefault(2, length);
                    int step = delimiters.getOrDefault(3, 1);

                    if (delimiters.isEmpty()) {
                        step();
                        return evaluate(root);
                    } else {
                        JArray subArray = new JArray();
                        if (step == 1) {
                            subArray.addAll(root.value(JArray.class).subList(start, stop));
                        } else {
                            IntStream.range(start, stop).forEach(i -> {
                                if (i % step == 0) {
                                    subArray.add(root.value(JArray.class).get(i));
                                }
                            });
                        }
                        step();
                        return evaluate(new JValue(subArray));
                    }
                }
                case STAR: {
                    Token before = tokens.get(current - 1);
                    Token after = tokens.get(current + 1);

                    if (before.type == TokenType.START_IDX && after.type == TokenType.END_IDX) {
                        step();
                        expectToken(TokenType.PERIOD);
                        expectToken(TokenType.IDENTIFIER);
                        String key = tokens.get(current).getValue().toString();
                        return projectionFilter(root, key, false);
                    } else if (before.type == TokenType.PERIOD && after.type == TokenType.PERIOD) {
                        expectToken(TokenType.PERIOD);
                        expectToken(TokenType.IDENTIFIER);
                        String key = tokens.get(current).getValue().toString();
                        JArray node = root.value(JObject.class).values().stream()
                                .map(value -> value.value(JObject.class).get(key))
                                .filter(Objects::nonNull)
                                .collect(Collectors.toCollection(JArray::new));
                        step();
                        return evaluate(new JValue(node));
                    } else {
                        step();
                        return evaluate(root);
                    }
                }
                case QUESTION_MARK: {
                    Token before = tokens.get(current - 1);
                    Token after = tokens.get(current + 1);

                    if (before.type == TokenType.START_IDX && after.type == TokenType.IDENTIFIER) {
                        expectToken(TokenType.IDENTIFIER);
                        String identifier = tokens.get(current).getValue().toString();
                        expectEither(TokenType.EQUAL_TO, TokenType.NOT_EQUAL_TO, TokenType.GREATER_OR_EQUAL_TO, TokenType.GREATER_THAN, TokenType.LESS_OR_EQUAL_TO, TokenType.LESS_OR_EQUAL_TO);
                        Token operator = tokens.get(current);
                        expectEither(TokenType.LITERAL, TokenType.NUMBER);
                        String literal = tokens.get(current).getValue().toString();
                        JArray node = root.value(JArray.class).stream().filter(value ->
                                        comparison(value, operator.type, identifier, literal))
                                .collect(Collectors.toCollection(JArray::new));
                        step();
                        return evaluate(new JValue(node));
                    }
                }
                case PIPE: {
                    System.out.println("Handing current result to a new evaluation pipeline");
                    return new JEval(tokens.subList(current + 1, tokens.size())).evaluate(root);
                }
            }
        }

        return root;
    }

    private JNode builtInFunction(JNode node, BuiltIn symbol) {
        switch (symbol) {
            case ABS: {
                expectToken(TokenType.OPEN_PAREN);
                expectToken(TokenType.IDENTIFIER);
                String identifier = tokens.get(current).getValue().toString();
                expectToken(TokenType.CLOSE_PAREN);

                BigDecimal number = node.value(JObject.class).get(identifier).value(BigDecimal.class);
                return new JValue(number.abs());
            }
            case AVG: {
                expectToken(TokenType.OPEN_PAREN);
                expectToken(TokenType.IDENTIFIER);
                String identifier = tokens.get(current).getValue().toString();
                expectToken(TokenType.CLOSE_PAREN);

                JNode array = node.value(JObject.class).get(identifier);
                BigDecimal sum = array.value(JArray.class).stream()
                        .map(v -> v.value(BigDecimal.class))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                return new JValue(sum.divide(new BigDecimal(array.value(JArray.class).size()), RoundingMode.CEILING));
            }
            case CEIL: {
                expectToken(TokenType.OPEN_PAREN);
                expectToken(TokenType.LITERAL);
                BigDecimal number = new BigDecimal(tokens.get(current).getValue().toString());
                expectToken(TokenType.CLOSE_PAREN);

                return new JValue(Math.ceil(number.doubleValue()));
            }
            case FLOOR: {
                expectToken(TokenType.OPEN_PAREN);
                expectToken(TokenType.LITERAL);
                BigDecimal number = new BigDecimal(tokens.get(current).getValue().toString());
                expectToken(TokenType.CLOSE_PAREN);

                return new JValue(Math.floor(number.doubleValue()));
            }
            case JOIN: {
                expectToken(TokenType.OPEN_PAREN);
                expectToken(TokenType.LITERAL);
                String glue = tokens.get(current).getValue().toString();

                JNode array = node;
                step();
                if (tokens.get(current).type == TokenType.COMMA) {
                    expectToken(TokenType.IDENTIFIER);
                    String identifier = tokens.get(current).getValue().toString();
                    expectToken(TokenType.CLOSE_PAREN);
                    array = node.value(JObject.class).get(identifier);
                }

                return new JValue(array.value(JArray.class).stream()
                        .map(v -> v.value(String.class))
                        .collect(Collectors.joining(glue)));
            }
            case KEYS: {
                expectToken(TokenType.OPEN_PAREN);
                expectToken(TokenType.CLOSE_PAREN);

                JArray keys = node.value(JObject.class).keySet()
                        .stream().map(JValue::new)
                        .collect(Collectors.toCollection(JArray::new));
                return new JValue(keys);
            }
            case CONTAINS: {
                expectToken(TokenType.OPEN_PAREN);
                expectEither(TokenType.LITERAL, TokenType.NUMBER);
                String identifier = tokens.get(current).getValue().toString();
                expectToken(TokenType.CLOSE_PAREN);

                if (node.is(String.class)) {
                    return new JValue(node.value(String.class).contains(identifier));
                } else if (node.is(JArray.class)) {
                    return new JValue(node.value(JArray.class).stream()
                            .map(v -> v.is(String.class)
                                    ? v.value(String.class)
                                    : v.value(BigDecimal.class).toString())
                            .anyMatch(v -> v.contains(identifier)));
                } else {
                    throw new RuntimeException("'contains' applies to only (1) a string value or (2) an array of string or numeric values");
                }
            }
            case ENDS_WITH: {
                expectToken(TokenType.OPEN_PAREN);
                step();
                List<Token> selector = new LinkedList<>();
                while (tokens.get(current).type != TokenType.COMMA) {
                    selector.add(tokens.get(current));
                    step();
                }

                JNode value = node;
                if (!selector.isEmpty()) {
                    value = new JEval(selector).evaluate(node);
                }

                expectToken(TokenType.LITERAL);
                String suffix = tokens.get(current).getValue().toString();
                expectToken(TokenType.CLOSE_PAREN);

                if (!value.is(String.class)) {
                    throw new RuntimeException("Expecting a node of string type");
                } else {
                    return new JValue(value.value(String.class).endsWith(suffix));
                }
            }
            case LENGTH: {
                expectToken(TokenType.OPEN_PAREN);
                expectToken(TokenType.IDENTIFIER);
                String identifier = tokens.get(current).getValue().toString();
                expectToken(TokenType.CLOSE_PAREN);

                JNode value = node.value(JObject.class).get(identifier);
                if (value.is(String.class)) {
                    return new JValue(new BigDecimal(value.value(String.class).length()));
                } else if (value.isArray()) {
                    return new JValue(new BigDecimal(value.value(JArray.class).size()));
                } else {
                    new JValue(new BigDecimal(value.value(JObject.class).size()));
                }
            }
            case MAP: {
                expectToken(TokenType.OPEN_PAREN);
                expectToken(TokenType.AMPERSAND);
                step();
                List<Token> selector = new LinkedList<>();
                while (tokens.get(current).type != TokenType.COMMA &&
                        tokens.get(current).type != TokenType.CLOSE_PAREN) {
                    selector.add(tokens.get(current));
                    step();
                }

                if (tokens.get(current).type == TokenType.COMMA) {
                    expectToken(TokenType.IDENTIFIER);
                    String identifier = tokens.get(current).getValue().toString();
                    expectToken(TokenType.CLOSE_PAREN);

                    //retrieve target node value
                    JNode value = node.value(JObject.class).get(identifier);
                    //simulate projection
                    List<Token> simulated = Stream.concat(Stream.of(
                            new Token(TokenType.START_IDX),
                            new Token(TokenType.END_IDX),
                            new Token(TokenType.PERIOD)), selector.stream()).collect(Collectors.toCollection(LinkedList::new));
                    return new JEval(simulated, false).evaluate(value);
                } else {
                    //TODO map function not yet fully implemented
                }
            }
            case MAX: {
                expectToken(TokenType.OPEN_PAREN);
                step();
                JNode array = node;
                if (tokens.get(current).type == TokenType.IDENTIFIER) {
                    String identifier = tokens.get(current).getValue().toString();
                    array = node.value(JObject.class).get(identifier);
                    expectToken(TokenType.CLOSE_PAREN);
                }

                return new JValue(array.value(JArray.class).stream().map(v -> {
                    if (v.is(String.class)) {
                        return v.value(String.class);
                    } else {
                        return v.value(BigDecimal.class);
                    }
                }).max((a, b) -> ((Comparable) a).compareTo(b)).orElseThrow());
            }
            case MAX_BY: {
                return null;
            }
            default:
                throw new RuntimeException(String.format("Unknown function - %s", symbol));
        }
    }

    private void step() {
        current++;
    }

    private Token peek() {
        if (current + 1 < tokens.size()) {
            return tokens.get(current + 1);
        }
        return null;
    }

    private JNode projectionFilter(JNode root, String key, boolean flatten) {
        JArray filtered = flatten ? applyFlatMap(root, key) : applyMap(root, key);
        step();
        return evaluate(new JValue(filtered));
    }

    private JArray applyFlatMap(JNode root, String key) {
        return root.value(JArray.class).stream()
                .flatMap(node -> {
                    if (node.isObject()) {
                        var obj = node.value(JObject.class);
                        return Stream.of(obj.get(key));
                    } else {
                        // has to be an array at this point
                        return applyFlatMap(node, key).stream();
                    }
                })
                .filter(v -> !filterNull || Objects.nonNull(v))
                .collect(Collectors.toCollection(JArray::new));
    }

    private JArray applyMap(JNode root, String key) {
        return root.value(JArray.class).stream()
                .map(node -> {
                    if (node.isObject()) {
                        var obj = node.value(JObject.class);
                        return obj.get(key);
                    } else {
                        // has to be an array at this point
                        return new JValue(applyMap(node, key));
                    }
                })
                .filter(v -> !filterNull || Objects.nonNull(v))
                .collect(Collectors.toCollection(JArray::new));
    }

    private void expectToken(TokenType type) {
        Token token = tokens.get(current + 1);
        if (token.type != type) {
            throw new RuntimeException(String.format("Expected a %s token but found %s", type, token.type));
        }
        step();
    }

    private void expectEither(TokenType... types) {
        Token token = tokens.get(current + 1);
        boolean matchFound = false;
        for (TokenType type : types) {
            if (token.type == type) {
                matchFound = true;
                break;
            }
        }
        if (!matchFound) {
            throw new RuntimeException(String.format("Expected one of %s", Arrays.stream(types).map(Enum::toString)));
        }
        step();
    }

    private boolean comparison(JNode value, TokenType operator, String property, String literal) {
        switch (operator) {
            case EQUAL_TO:
                return compareEquality(value, property, literal);
            case NOT_EQUAL_TO:
                return compareInequality(value, property, literal);
            case GREATER_THAN:
                return compareGreaterThan(value, property, literal);
            case GREATER_OR_EQUAL_TO:
                return compareGreaterOrEqualTo(value, property, literal);
            case LESS_THAN:
                return compareLessThan(value, property, literal);
            case LESS_OR_EQUAL_TO:
                return compareLessOrEqualTo(value, property, literal);
            default:
                throw new RuntimeException(String.format("Unexpected comparison operator - %s", operator));
        }
    }
}
