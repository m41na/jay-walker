package works.hop.json.walk;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Token {

    TokenType type;
    Object value;

    public Token(TokenType type) {
        this.type = type;
    }
}
