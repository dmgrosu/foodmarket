package md.ramaiana.foodmarket.model;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Table("app_user_role")
public class AppRole {
    @Id
    private final Integer id;
    @Column("user_id")
    private final Integer userId;
    private final Role role;

    @PersistenceCreator
    public AppRole(Integer id, Integer userId, Role role) {
        this.id = id;
        this.userId = userId;
        this.role = role;
    }

    public AppRole(Integer userId, Role role) {
        this.id = null;
        this.userId = userId;
        this.role = role;
    }
}
