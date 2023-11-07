package works.hop.json.walk;

import works.hop.json.JParser;
import works.hop.json.api.JNode;
import works.hop.json.ops.BuiltIn;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

public class JWalker {

    final JNode root;
    int current = 0;
    String path;

    public JWalker(String input) {
        this.root = new JParser(input).parse();
    }

    public JNode walk(String path) {
        this.path = path;
        List<Token> tokens = new LinkedList<>();

        while (current < path.length()) {
            char ch = path.charAt(current);
            if (Character.isWhitespace(ch)) {
                step();
            } else if (ch == ',') {
                step();
                tokens.add(new Token(TokenType.COMMA));
            } else if (ch == '.') {
                step();
                tokens.add(new Token(TokenType.PERIOD));
            } else if (ch == '[') {
                step();
                tokens.add(new Token(TokenType.START_IDX));
            } else if (ch == ']') {
                step();
                tokens.add(new Token(TokenType.END_IDX));
            } else if (ch == '{') {
                step();
                tokens.add(new Token(TokenType.LEFT_CURLY));
            } else if (ch == '}') {
                step();
                tokens.add(new Token(TokenType.RIGHT_CURLY));
            } else if (ch == '(') {
                step();
                tokens.add(new Token(TokenType.OPEN_PAREN));
            } else if (ch == ')') {
                step();
                tokens.add(new Token(TokenType.CLOSE_PAREN));
            } else if (ch == ':') {
                step();
                tokens.add(new Token(TokenType.COLON));
            } else if (ch == '*') {
                step();
                tokens.add(new Token(TokenType.STAR));
            } else if (ch == '?') {
                step();
                tokens.add(new Token(TokenType.QUESTION_MARK));
            } else if (ch == '|') {
                step();
                tokens.add(new Token(TokenType.PIPE));
            } else if (ch == '&') {
                step();
                tokens.add(new Token(TokenType.AMPERSAND));
            } else if (Character.isAlphabetic(ch) || ch == '_') {
                identifier(tokens);
            } else if (Character.isDigit(ch)) {
                BigDecimal number = numeral();
                tokens.add(new Token(TokenType.NUMBER, number));
            } else if (ch == '\'') {
                String literal = literal();
                tokens.add(new Token(TokenType.LITERAL, literal));
            } else if (ch == '=') {
                if (peek() == '=') {
                    step();
                    tokens.add(new Token(TokenType.EQUAL_TO));
                }
                step();
                System.out.printf("skipping unexpected character after '=' - '%s'\n", ch);
            } else if (ch == '!') {
                if (peek() == '=') {
                    step();
                    tokens.add(new Token(TokenType.NOT_EQUAL_TO));
                }
                step();
                System.out.printf("skipping unexpected character after '!' - '%s'\n", ch);
            } else if (ch == '>') {
                if (peek() == '=') {
                    step();
                    tokens.add(new Token(TokenType.GREATER_OR_EQUAL_TO));
                } else {
                    step();
                    tokens.add(new Token(TokenType.GREATER_THAN));
                }
            } else if (ch == '<') {
                if (peek() == '=') {
                    step();
                    tokens.add(new Token(TokenType.LESS_OR_EQUAL_TO));
                } else {
                    step();
                    tokens.add(new Token(TokenType.LESS_THAN));
                }
            } else {
                step();
                System.out.printf("skipping unexpected character - '%s'\n", ch);
            }
        }

        return evaluate(tokens);
    }

    private JNode evaluate(List<Token> tokens) {
        return new JEval(tokens).evaluate(this.root);
    }

    private void step() {
        this.current++;
    }

    private boolean hasMore() {
        return (current + 1 < path.length());
    }

    private char peek() {
        return this.path.charAt(current + 1);
    }

    private char prev() {
        return this.path.charAt(current - 1);
    }

    private void identifier(List<Token> tokens) {
        int start = current;
        while (hasMore() && (Character.isLetterOrDigit(peek()) || peek() == '_')) {
            step();
        }
        step();
        var value = this.path.substring(start, current);
        if (BuiltIn.functions().contains(value)) {
            tokens.add(new Token(TokenType.FUNCTION, BuiltIn.of(value)));
        } else {
            tokens.add(new Token(TokenType.IDENTIFIER, value));
        }
    }

    private String literal() {
        int start = current;
        step();
        while (hasMore() && path.charAt(current) != '\'' && prev() != '\\') {
            step();
        }
        step();
        return this.path.substring(start + 1, current - 1);
    }

    private BigDecimal numeral() {
        int start = current;

        while (hasMore() && Character.isDigit(peek()) || peek() == '.') {
            step();
            char ch = path.charAt(current);
            if (ch == '.' && !Character.isDigit(peek())) {
                throw new RuntimeException("Expected a digit character after the decimal point");
            }
        }
        step();
        return new BigDecimal(this.path.substring(start, current));
    }
}
