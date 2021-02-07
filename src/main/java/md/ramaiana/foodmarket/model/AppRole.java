package md.ramaiana.foodmarket.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * @author Dmitri Grosu, 2/6/21
 */
@Getter
@AllArgsConstructor
@Builder
@Table("app_user_role")
public class AppRole {
    @Id
    Integer id;
    @Column("user_id")
    Integer userId;
    Role role;
}
