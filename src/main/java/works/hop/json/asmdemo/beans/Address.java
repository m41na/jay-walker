package works.hop.json.asmdemo.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Address {

    String streetName;
    String unit;
    String city;
    String state;
    String zip;
}
