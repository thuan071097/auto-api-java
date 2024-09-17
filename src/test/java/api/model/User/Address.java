package api.model.User;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder

public class Address {
    private String streetNumber;
    private String street;
    private String ward;
    private String district;
    private String city;
    private String state;
    private String zip;
    private String country;

    public static Address getDefault() {
        return  Address.builder()
                .streetNumber("123")
                .street("Main St")
                .ward("Ward 1")
                .district("District 1")
                .city("Thu Duc")
                .state("Ho Chi Minh")
                .zip("70000")
                .country("VN")
                .build();
    }

    /*private boolean flag;
    private int someNumber;
    -> kho khan trong vc test requied field ==> nen de String
    Chu y cac kieu DL cho input
    */
}
