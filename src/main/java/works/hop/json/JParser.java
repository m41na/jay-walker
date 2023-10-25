package works.hop.json;

import lombok.RequiredArgsConstructor;
import works.hop.json.api.JArray;
import works.hop.json.api.JInput;
import works.hop.json.api.JNode;
import works.hop.json.api.JObject;
import works.hop.json.api.JValue;

import javax.json.Json;
import javax.json.stream.JsonParser;

import java.io.StringReader;
import java.util.Stack;

@RequiredArgsConstructor
public class JParser {

    final String input;

    public static JNode parse(String filePath){
        JInput input = (content) -> new JParser(content).parse();
        return input.resource(filePath);
    }

    public JNode parse() {
        Stack<JNode> pipe = new Stack<>();
        Stack<String> keys = new Stack<>();

        StringReader data = new StringReader(input);
        JsonParser parser = Json.createParser(data);

        while (parser.hasNext()) {
            JsonParser.Event e = parser.next();
            switch (e) {
                case START_OBJECT:
                    startNewValue(new JObject(), pipe);
                    break;
                case START_ARRAY:
                    startNewValue(new JArray(), pipe);
                    break;
                case KEY_NAME:
                    keys.push(parser.getString());
                    break;
                case VALUE_NULL: {
                    String key = currentKey(keys);
                    System.out.printf("The value for %s is null\n", key);
                    break;
                }
                case VALUE_STRING:
                    if (topIsEmpty(pipe)) {
                        throw new RuntimeException("Expecting a parent object or list");
                    }
                    if (topIsObject(pipe)) {
                        String key = currentKey(keys);
                        setValueInObject(key, parser.getString(), pipe);
                        continue;
                    }
                    if (topIsArray(pipe)) {
                        addValueToList(parser.getString(), pipe);
                        continue;
                    }
                    break;
                case VALUE_NUMBER:
                    if (topIsEmpty(pipe)) {
                        throw new RuntimeException("Expecting a parent object or list");
                    }
                    if (topIsObject(pipe)) {
                        String key = currentKey(keys);
                        setValueInObject(key, parser.getBigDecimal(), pipe);
                        continue;
                    }
                    if (topIsArray(pipe)) {
                        addValueToList(parser.getBigDecimal(), pipe);
                        continue;
                    }
                    break;
                case VALUE_TRUE:
                case VALUE_FALSE:
                    if (topIsEmpty(pipe)) {
                        throw new RuntimeException("Expecting a parent object or list");
                    }
                    if (topIsObject(pipe)) {
                        String key = currentKey(keys);
                        setValueInObject(key, e == JsonParser.Event.VALUE_TRUE, pipe);
                        continue;
                    }
                    if (topIsArray(pipe)) {
                        addValueToList(e == JsonParser.Event.VALUE_TRUE, pipe);
                        continue;
                    }
                    break;
                case END_ARRAY:
                case END_OBJECT:
                    if (pipe.size() == 1) {
                        return pipe.pop();
                    }

                    Object value = pipe.pop().value();
                    if (topIsObject(pipe)) {
                        String key = keys.pop();
                        setValueInObject(key, value, pipe);
                        continue;
                    }
                    if (topIsArray(pipe)) {
                        addValueToList(value, pipe);
                        continue;
                    }
                    break;
            }
        }

        return pipe.pop();
    }

    private void startNewValue(Object value, Stack<JNode> pipe) {
        JNode newNode = new JValue(value);
        pipe.push(newNode); //pipe has a new object at the top
    }

    private void addValueToList(Object value, Stack<JNode> pipe) {
        pipe.peek().value(JArray.class).add(new JValue(value));
    }

    private void setValueInObject(String key, Object value, Stack<JNode> pipe) {
        pipe.peek().value(JObject.class).put(key, new JValue(value));
    }

    private String currentKey(Stack<String> keys) {
        return keys.pop();
    }

    private boolean topIsEmpty(Stack<JNode> pipe) {
        return pipe.isEmpty();
    }

    private boolean topIsArray(Stack<JNode> pipe) {
        return pipe.peek().isArray();
    }

    private boolean topIsObject(Stack<JNode> pipe) {
        return pipe.peek().isObject();
    }
}
