package api.model.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

public class Address {
    private String streetNumber;
    private String street;
    private String ward;
    private String district;
    private String city;
    private String state;
    private String zip;
    private String country;
    /*private boolean flag;
    private int someNumber;
    -> kho khan trong vc test requied field ==> nen de String
    Chu y cac kieu DL cho input
    */
}
