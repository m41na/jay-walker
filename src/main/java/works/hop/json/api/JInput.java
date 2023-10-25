package works.hop.json.api;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.function.Function;

public interface JInput extends Function<String, JNode> {

    default JNode resource(String fileName) {
        try{
            String content = Files.readString(Paths.get(Objects.requireNonNull(JInput.class.getClassLoader().getResource(fileName)).toURI()));
            return apply(content);
        } catch (IOException | URISyntaxException e) {
            try {
                String content = Files.readString(Paths.get(Objects.requireNonNull(JInput.class.getResource(fileName)).toURI()));
                return apply(content);
            } catch (IOException | URISyntaxException e1) {
                throw new RuntimeException(e1);
            }
        }
    }
}
