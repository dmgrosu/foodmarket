package md.ramaiana.foodmarket.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Set;

/**
 * @author Dmitri Grosu (dmitri.grosu@codefactorygroup.com), 2/6/21
 */
@AllArgsConstructor
@Getter
@Builder
@Table("app_user")
public class AppUser {
    @Id
    Integer id;
    String email;
    String passwd;
    @MappedCollection(idColumn = "user_id")
    Set<AppRole> roles;
}
