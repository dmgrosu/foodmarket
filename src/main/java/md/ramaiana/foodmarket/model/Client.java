package md.ramaiana.foodmarket.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

/**
 * @author Dmitri Grosu (dmitri.grosu@gmail.com), 2/7/21
 */
@AllArgsConstructor
@Builder
@Getter
@Table("client")
public class Client {
    @Id
    Integer id;
    String name;
    String idno;
    OffsetDateTime created_at;
    OffsetDateTime deleted_at;
}
