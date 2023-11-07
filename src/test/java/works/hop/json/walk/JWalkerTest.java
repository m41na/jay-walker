package works.hop.json.walk;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import works.hop.json.api.JArray;
import works.hop.json.api.JNode;
import works.hop.json.api.JObject;
import works.hop.json.api.JValue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class JWalkerTest {

    @Test
    void walk_for_basic_object_property_from_root_node() {
        //has the benefit of using the same node to walk different paths
        String input = "{\"a\": \"foo\", \"b\": \"bar\", \"c\": \"baz\"}";

        JNode nodeA = new JWalker(input).walk("a");
        assertThat(nodeA).isNotNull();
        assertThat(nodeA.value(String.class)).isEqualTo("foo");

        JNode nodeB = new JWalker(input).walk("b");
        assertThat(nodeB).isNotNull();
        assertThat(nodeB.value(String.class)).isEqualTo("bar");

        JNode node = new JWalker(input).walk("c");
        assertThat(node).isNotNull();
        assertThat(node.value(String.class)).isEqualTo("baz");

        JNode nodeNull = new JWalker(input).walk("abc");
        assertThat(nodeNull).isNull();
    }

    @Test
    void walk_for_nested_object_property() {
        String input = "{\"a\": {\"b\": {\"c\": {\"d\": \"value\"}}}}";

        JNode node = new JWalker(input).walk("a.b.c.d");
        assertThat(node).isNotNull();
        assertThat(node.value(String.class)).isEqualTo("value");

        JNode nodeNull = new JWalker(input).walk("abc");
        assertThat(nodeNull).isNull();
    }

    @Test
    void walk_for_list_items() {
        String input = "[\"a\", \"b\", \"c\", \"d\", \"e\", \"f\"]";

        JNode node = new JWalker(input).walk("[1]");
        assertThat(node).isNotNull();
        assertThat(node.value(String.class)).isEqualTo("b");
    }

    @Test
    void walk_for_combination_of_nested_objects_and_list_items() {
        String input = "{\"a\": {\n" +
                "  \"b\": {\n" +
                "    \"c\": [\n" +
                "      {\"d\": [0, [1, 2]]},\n" +
                "      {\"d\": [3, 4]}\n" +
                "    ]\n" +
                "  }\n" +
                "}}";

        JNode node = new JWalker(input).walk("a.b.c[0].d[1][0]");
        assertThat(node).isNotNull();
        assertThat(node.value(BigDecimal::intValue)).isEqualTo(1);
    }

    @Test
    void walk_with_slicing() {
        String input = "[0, 1, 2, 3, 4, 5, 6, 7, 8, 9]";

        JNode node = new JWalker(input).walk("[0:5]");
        assertThat(node).isNotNull();
        int i = 0;
        for (Object value : node.value(JArray.class)) {
            assertThat(((JValue) value).value(BigDecimal::intValue)).isEqualTo(i);
            i++;
        }

        JNode node2 = new JWalker(input).walk("[5:10]");
        assertThat(node2).isNotNull();
        int j = 5;
        for (Object value : node2.value(JArray.class)) {
            assertThat(((JValue) value).value(BigDecimal::intValue)).isEqualTo(j);
            j++;
        }

        JNode node3 = new JWalker(input).walk("[:5]");
        assertThat(node3).isNotNull();
        int k = 0;
        for (Object value : node3.value(JArray.class)) {
            assertThat(((JValue) value).value(BigDecimal::intValue)).isEqualTo(k);
            k++;
        }

        JNode node4 = new JWalker(input).walk("[5:]");
        assertThat(node4).isNotNull();
        int l = 5;
        for (Object value : node4.value(JArray.class)) {
            assertThat(((JValue) value).value(BigDecimal::intValue)).isEqualTo(l);
            l++;
        }
    }

    @Test
    void walk_with_slicing_and_stepping() {
        String input = "[0, 1, 2, 3, 4, 5, 6, 7, 8, 9]";

        JNode node = new JWalker(input).walk("[::1]");
        assertThat(node).isNotNull();
        List<Integer> values = node.value(JArray.class).stream().map(v -> v.value(BigDecimal.class).intValue()).collect(Collectors.toList());
        assertThat(values).hasSize(10);
        assertThat(values).containsAll(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9));

        JNode node2 = new JWalker(input).walk("[::2]");
        assertThat(node2).isNotNull();
        List<Integer> values2 = node2.value(JArray.class).stream().map(v -> v.value(BigDecimal.class).intValue()).collect(Collectors.toList());
        assertThat(values2).hasSize(5);
        assertThat(values2).containsAll(List.of(0, 2, 4, 6, 8));

        JNode node2_1 = new JWalker(input).walk("[3::2]");
        assertThat(node2_1).isNotNull();
        List<Integer> values2_1 = node2_1.value(JArray.class).stream().map(v -> v.value(BigDecimal.class).intValue()).collect(Collectors.toList());
        assertThat(values2_1).hasSize(3);
        assertThat(values2_1).containsAll(List.of(4, 6, 8));

        JNode node3 = new JWalker(input).walk("[::3]");
        assertThat(node3).isNotNull();
        List<Integer> values3 = node3.value(JArray.class).stream().map(v -> v.value(BigDecimal.class).intValue()).collect(Collectors.toList());
        assertThat(values3).hasSize(4);
        assertThat(values3).containsAll(List.of(0, 3, 6, 9));
    }

    @Test
    void projection_with_a_wildcard() {
        String input = "{\n" +
                "  \"people\": [\n" +
                "    {\"first\": \"James\", \"last\": \"d\"},\n" +
                "    {\"first\": \"Jacob\", \"last\": \"e\"},\n" +
                "    {\"first\": \"Jayden\", \"last\": \"f\"},\n" +
                "    {\"missing\": \"different\"}\n" +
                "  ],\n" +
                "  \"foo\": {\"bar\": \"baz\"}\n" +
                "}";

        JNode node = new JWalker(input).walk("people[*].first");
        assertThat(node).isNotNull();
        List<String> values = node.value(JArray.class).stream().map(v -> v.value().toString()).collect(Collectors.toList());
        assertThat(values).containsAll(List.of("James", "Jacob", "Jayden"));
    }

    @Test
    void projection_with_a_list_slice() {
        String input = "{\n" +
                "  \"people\": [\n" +
                "    {\"first\": \"James\", \"last\": \"d\"},\n" +
                "    {\"first\": \"Jacob\", \"last\": \"e\"},\n" +
                "    {\"first\": \"Jayden\", \"last\": \"f\"},\n" +
                "    {\"missing\": \"different\"}\n" +
                "  ],\n" +
                "  \"foo\": {\"bar\": \"baz\"}\n" +
                "}";

        JNode node = new JWalker(input).walk("people[:2].first");
        assertThat(node).isNotNull();
        List<String> values = node.value(JArray.class).stream().map(v -> v.value().toString()).collect(Collectors.toList());
        assertThat(values).containsAll(List.of("James", "Jacob"));
    }

    @Test
    void projection_with_an_object() {
        String input = "{\n" +
                "  \"ops\": {\n" +
                "    \"functionA\": {\"numArgs\": 2},\n" +
                "    \"functionB\": {\"numArgs\": 3},\n" +
                "    \"functionC\": {\"variadic\": true}\n" +
                "  }\n" +
                "}";

        JNode node = new JWalker(input).walk("ops.*.numArgs");
        assertThat(node).isNotNull();
        List<Integer> values = node.value(JArray.class).stream().map(v -> Integer.parseInt(v.value().toString())).collect(Collectors.toList());
        assertThat(values).containsAll(List.of(2, 3));
    }

    @Test
    void chain_of_projections() {
        String input = "{\n" +
                "  \"reservations\": [\n" +
                "    {\n" +
                "      \"instances\": [\n" +
                "        {\"state\": \"running\"},\n" +
                "        {\"state\": \"stopped\"}\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"instances\": [\n" +
                "        {\"state\": \"terminated\"},\n" +
                "        {\"state\": \"running\"}\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        JNode node = new JWalker(input).walk("reservations[*].instances[*].state");
        assertThat(node).isNotNull();
        List<List<String>> values = node.value(JArray.class).stream()
                .map(v -> v.value(JArray.class).stream()
                        .map(n -> n.value(String.class))
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
        assertThat(values).containsAll(List.of(List.of("running", "stopped"), List.of("terminated", "running")));
    }

    @Test
    void flatten_chain_of_projections() {
        String input = "{\n" +
                "  \"reservations\": [\n" +
                "    {\n" +
                "      \"instances\": [\n" +
                "        {\"state\": \"running\"},\n" +
                "        {\"state\": \"stopped\"}\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"instances\": [\n" +
                "        {\"state\": \"terminated\"},\n" +
                "        {\"state\": \"running\"}\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        JNode node = new JWalker(input).walk("reservations[].instances[].state");
        assertThat(node).isNotNull();
        List<String> values = node.value(JArray.class).stream().map(v -> v.value(String.class)).collect(Collectors.toList());
        assertThat(values).containsAll(List.of("running", "stopped", "terminated", "running"));
    }

    @Test
    void filter_projections_with_string_operands() {
        String input = "{\n" +
                "  \"machines\": [\n" +
                "    {\"name\": \"a\", \"state\": \"running\"},\n" +
                "    {\"name\": \"b\", \"state\": \"stopped\"},\n" +
                "    {\"name\": \"c\", \"state\": \"running\"}\n" +
                "  ]\n" +
                "}";

        JNode node = new JWalker(input).walk("machines[?state=='running'].name");
        assertThat(node).isNotNull();
        List<String> values = node.value(JArray.class).stream().map(v -> v.value(String.class)).collect(Collectors.toList());
        assertThat(values).containsAll(List.of("a", "c"));
    }

    @Test
    void filter_projections_with_numeric_operands() {
        String input = "{\n" +
                "  \"machines\": [\n" +
                "    {\"name\": \"a\", \"number\": 2},\n" +
                "    {\"name\": \"b\", \"number\": 0},\n" +
                "    {\"name\": \"c\", \"number\": 2},\n" +
                "    {\"name\": \"d\", \"number\": 1}\n" +
                "  ]\n" +
                "}";

        JNode node1 = new JWalker(input).walk("machines[?number > 0].name");
        assertThat(node1).isNotNull();
        List<String> values1 = node1.value(JArray.class).stream().map(v -> v.value(String.class)).collect(Collectors.toList());
        assertThat(values1).containsAll(List.of("a", "c"));

        JNode node2 = new JWalker(input).walk("machines[?number == 0].name");
        assertThat(node2).isNotNull();
        List<String> values2 = node2.value(JArray.class).stream().map(v -> v.value(String.class)).collect(Collectors.toList());
        assertThat(values2).containsAll(List.of("b"));

        JNode node3 = new JWalker(input).walk("machines[?number <= 1].name");
        assertThat(node3).isNotNull();
        List<String> values3 = node3.value(JArray.class).stream().map(v -> v.value(String.class)).collect(Collectors.toList());
        assertThat(values3).containsAll(List.of("b", "d"));
    }

    @Test
    void piping_operations() {
        String input = "{\n" +
                "  \"people\": [\n" +
                "    {\"first\": \"James\", \"last\": \"d\"},\n" +
                "    {\"first\": \"Jacob\", \"last\": \"e\"},\n" +
                "    {\"first\": \"Jayden\", \"last\": \"f\"},\n" +
                "    {\"missing\": \"different\"}\n" +
                "  ],\n" +
                "  \"foo\": {\"bar\": \"baz\"}\n" +
                "}";

        JNode node = new JWalker(input).walk("people[*].first | [0]");
        assertThat(node).isNotNull();
        String result = node.value(String.class);
        assertThat(result).isEqualTo("James");
    }

    @Test
    void multiselect_list() {
        String input = "{\n" +
                "  \"people\": [\n" +
                "    {\n" +
                "      \"name\": \"a\",\n" +
                "      \"state\": {\"name\": \"up\"}\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"b\",\n" +
                "      \"state\": {\"name\": \"down\"}\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"c\",\n" +
                "      \"state\": {\"name\": \"up\"}\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        JNode node = new JWalker(input).walk("people[].[name, state.name]");
        assertThat(node).isNotNull();
        List<List<String>> values = node.value(JArray.class).stream()
                .map(v -> v.value(JArray.class).stream()
                        .map(x -> x.value(String.class))
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
        assertThat(values).containsAll(List.of(List.of("a", "up"), List.of("b", "down"), List.of("c", "up")));
    }

    @Test
    void multiselect_hash() {
        String input = "{\n" +
                "  \"people\": [\n" +
                "    {\n" +
                "      \"name\": \"a\",\n" +
                "      \"state\": {\"name\": \"up\"}\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"b\",\n" +
                "      \"state\": {\"name\": \"down\"}\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"c\",\n" +
                "      \"state\": {\"name\": \"up\"}\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        JNode node = new JWalker(input).walk("people[].{Name: name, State: state.name}");
        assertThat(node).isNotNull();
        List<Map<String, String>> values = node.value(JArray.class).stream()
                .map(e -> e.value(JObject.class).keySet().stream()
                        .collect(Collectors.toMap(k -> k, k -> e.value(JObject.class).get(k).value(String.class))))
                .collect(Collectors.toList());
        assertThat(values).containsAll(List.of(Map.of("Name", "a", "State", "up"), Map.of("Name", "b", "State", "down"), Map.of("Name", "c", "State", "up")));
    }

    @Test
    void functions_abs() {
        String input = "{\"foo\": -1, \"bar\": \"2\"}";

        JNode node = new JWalker(input).walk("abs(foo)");
        assertThat(node).isNotNull();
        int size = node.value(BigDecimal.class).intValue();
        assertThat(size).isEqualTo(1);
    }

    @Test
    void functions_avg() {
        String input = "{\"foo\": -1, \"bar\": [10, 15, 20]}";

        JNode node = new JWalker(input).walk("avg(bar)");
        assertThat(node).isNotNull();
        int size = node.value(BigDecimal.class).intValue();
        assertThat(size).isEqualTo(15);
    }

    @Test
    void functions_contains_string() {
        String input = "[\"a\", \"b\"]";

        JNode node = new JWalker(input).walk("contains('a')");
        assertThat(node).isNotNull();
        boolean exists = node.value(Boolean.class);
        assertThat(exists).isTrue();
    }

    @Test
    void functions_contains_number() {
        String input = "[2, 4, 6]";

        JNode node = new JWalker(input).walk("contains(4)");
        assertThat(node).isNotNull();
        boolean exists = node.value(Boolean.class);
        assertThat(exists).isTrue();
    }

    @Test
    void functions_ceil() {
        String input = "[]";

        JNode node = new JWalker(input).walk("ceil('1.001')");
        assertThat(node).isNotNull();
        Double rounded = node.value(Double.class);
        assertThat(rounded.intValue()).isEqualTo(2);
    }

    @Test
    void functions_ends_with() {
        String input = "[\"foorbarbaz\"]";

        JNode node = new JWalker(input).walk("ends_with([0], 'baz')");
        assertThat(node).isNotNull();

        Boolean isTrue = node.value(Boolean.class);
        assertThat(isTrue).isTrue();

        JNode node2 = new JWalker(input).walk("ends_with([0], 'foo')");
        assertThat(node2).isNotNull();
        Boolean isFalse = node2.value(Boolean.class);
        assertThat(isFalse).isFalse();
    }

    @Test
    void functions_floor() {
        String input = "[]";

        JNode node = new JWalker(input).walk("floor('1.001')");
        assertThat(node).isNotNull();
        Double rounded = node.value(Double.class);
        assertThat(rounded.intValue()).isEqualTo(1);
    }

    @Test
    void functions_join() {
        String input = "{\"array\": [\"a\", \"b\", \"c\"]}";

        JNode node = new JWalker(input).walk("join(', ', array)");
        assertThat(node).isNotNull();
        String joined = node.value(String.class);
        assertThat(joined).isEqualTo("a, b, c");

        String input2 = "[\"a\", \"b\", \"c\"]";

        JNode node2 = new JWalker(input2).walk("join(', ')");
        assertThat(node2).isNotNull();
        String joined2 = node2.value(String.class);
        assertThat(joined2).isEqualTo("a, b, c");
    }

    @Test
    void functions_keys() {
        String input = "{\"foo\": \"baz\", \"bar\": \"bam\"}";

        JNode node = new JWalker(input).walk("keys()");
        assertThat(node).isNotNull();
        List<String> keys = node.value(JArray.class).stream().map(v -> v.value(String.class)).collect(Collectors.toList());
        assertThat(keys).containsAll(List.of("foo", "bar"));
    }

    @Test
    void functions_length() {
        String input = "{\n" +
                "  \"people\": [\n" +
                "    {\n" +
                "      \"name\": \"b\",\n" +
                "      \"age\": 30,\n" +
                "      \"state\": {\"name\": \"up\"}\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"a\",\n" +
                "      \"age\": 50,\n" +
                "      \"state\": {\"name\": \"down\"}\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"c\",\n" +
                "      \"age\": 40,\n" +
                "      \"state\": {\"name\": \"up\"}\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        JNode node = new JWalker(input).walk("length(people)");
        assertThat(node).isNotNull();
        int size = node.value(BigDecimal.class).intValue();
        assertThat(size).isEqualTo(3);
    }

    @Test
    void functions_map_using_object_projection() {
        String input = "{\"array\": [{\"foo\": \"a\"}, {\"foo\": \"b\"}, {}, [], {\"foo\": \"f\"}]}";

        JNode node = new JWalker(input).walk("map(&foo, array)");
        assertThat(node).isNotNull();
        assertThat(node.value(JArray.class).stream().map(v -> v != null ? v.value(String.class) : null)).containsAll(List.of("a", "b", "f"));
    }

    @Test
    @Disabled
    void functions_map_using_list_projection() {
        String input = "[[1, 2, 3, [4]], [5, 6, 7, [8, 9]]]";

        JNode node = new JWalker(input).walk("map(&[])");
        assertThat(node).isNotNull();
        assertThat(node.value(String.class)).isEqualTo("[[1, 2, 3, 4], [5, 6, 7, 8, 9]]");
    }

    @Test
    void functions_max() {
        String input = "{\"array\": [10, 15]}";

        JNode node = new JWalker(input).walk("max(array)");
        assertThat(node).isNotNull();
        assertThat(node.value(BigDecimal.class).intValue()).isEqualTo(15);

        String input2 = "[\"a\", \"b\"]";

        JNode node2 = new JWalker(input2).walk("max()");
        assertThat(node2).isNotNull();
        assertThat(node2.value(String.class)).isEqualTo("b");
    }

    @Test
    void functions_max_by() {
        String input = "{\"array\": [10, 15]}";

        JNode node = new JWalker(input).walk("max(array)");
        assertThat(node).isNotNull();
        assertThat(node.value(BigDecimal.class).intValue()).isEqualTo(15);

        String input2 = "[\"a\", \"b\"]";

        JNode node2 = new JWalker(input2).walk("max()");
        assertThat(node2).isNotNull();
        assertThat(node2.value(String.class)).isEqualTo("b");
    }
}