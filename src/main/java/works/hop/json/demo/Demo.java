package works.hop.json.demo;

import org.apache.commons.cli.*;
import works.hop.json.api.JNode;
import works.hop.json.walk.JWalker;

/**
 * Example usage (using bash terminal)
 * java -jar build/libs/jjq-0.1.0.jar -json "{\"a\": \"foo\", \"b\": \"bar\", \"c\": \"baz\"}" -query "a"
 * should produce "foo"
 */
public class Demo {

    public static void main(String[] args) throws ParseException {
        // create Options object
        Options options = new Options();

        // add options
        options.addOption("j", "json", true, "json input");
        options.addOption("q", "query", true, "query input");

        // parse the cli arguments
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        // evaluate query expression
        JNode node = new JWalker(cmd.getOptionValue("json")).walk(cmd.getOptionValue("query"));
        System.out.println(node.value(String.class));
    }
}
