package works.hop.json.walk;

public enum TokenType {

    // content delimiters
    COMMA(","), PERIOD("."), START_IDX("["), END_IDX("]"), OPEN_PAREN("("), CLOSE_PAREN(")"), COLON(":"), STAR("*"), QUESTION_MARK("?"), PIPE("|"), AMPERSAND("&"),
    // comparison operators
    EQUAL_TO("=="), NOT_EQUAL_TO("!="), LESS_THAN("<"), LESS_OR_EQUAL_TO("<="), GREATER_THAN(">"), GREATER_OR_EQUAL_TO(">="),
    // value attributes
    IDENTIFIER(null), FUNCTION(null), NUMBER(null), LITERAL(null);

    public final String symbol;

    TokenType(String symbol) {
        this.symbol = symbol;
    }
}
