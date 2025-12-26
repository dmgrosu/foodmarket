package md.ramaiana.foodmarket.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@AllArgsConstructor
@Getter
@Builder
@Table("app_user_role")
public class AppRole {
    @Id
    @With
    private final Integer id;
    @Column("user_id")
    private final Integer userId;
    private final Role role;

}
