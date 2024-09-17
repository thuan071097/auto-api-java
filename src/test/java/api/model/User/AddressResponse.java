package api.model.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AddressResponse extends Address {
    private String id;
    private String customerId;
    private String createdAt;
    private String updatedAt;
    // tim hieu generic
}
