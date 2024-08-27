package api.model.country;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Sinh ra contructer default
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Country {
    private String  name;
    private String code;
}
