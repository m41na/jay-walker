package works.hop.json;

import org.junit.jupiter.api.Test;
import works.hop.json.api.JArray;
import works.hop.json.api.JNode;
import works.hop.json.api.JObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class JParserTest {

    private static void assertParsedJsonContent(JNode root) {
        assertThat(root).isNotNull();
        assertThat(root
                .value(JObject.class).get("phoneNumbers")
                .value(JArray.class).get(0)
                .value(JObject.class).get("number")
                .value(String.class)).isEqualTo("212 555-1234");
        assertThat(root
                .value(JObject.class).get("address")
                .value(JObject.class).get("ext")).isNull();
    }

    @Test
    void parse_should_return_valid_map() throws URISyntaxException, IOException {
        String input = Files.readString(
                Path.of(Objects.requireNonNull(getClass().getClassLoader().getResource("input.json")).toURI()));

        JNode root = new JParser(input).parse();
        assertParsedJsonContent(root);
    }

    @Test
    void parse_static_should_return_valid_map() throws URISyntaxException, IOException {
        JNode root = JParser.parse("input.json");
        assertParsedJsonContent(root);
    }
}
