package api.model.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Card {
    // request/response deu hung String (chi khi tuong tac voi DB can dung kieu DL): Phai test t/h null & empty
    private String userId;
    private String type;
}
