package works.hop.graph;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Trail implements Comparable<Trail> {

    Node current;
    Integer distance = Integer.MAX_VALUE;
    Node prev;

    public Trail(Node current, int distance) {
        this.current = current;
        this.distance = distance;
    }

    @Override
    public int compareTo(Trail o) {
        if (this == o) return 0;
        if (this.current == o.current) return 0;
        return this.distance.compareTo(o.distance);
    }
}
