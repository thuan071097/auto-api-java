package api.model.User.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "customers")
@Data
public class DbUser {
    @Id
    private UUID id;
    private String firstName;
    private String lastName;
    private String middleName;
    private String birthday;
    private String email;
    private String phone;
    @JsonSerialize(using = InstantSerializer.class)
    private Instant createdAt;
    @JsonSerialize(using = InstantSerializer.class)
    private Instant updatedAt;
}
