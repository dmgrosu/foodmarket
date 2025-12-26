package md.ramaiana.foodmarket.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.Objects;

@AllArgsConstructor
@Builder
@Getter
@Table("client")
public class Client {
    @Id
    @With
    private final Integer id;
    private final String name;
    private final String idno;
    @Column("created_at")
    @Builder.Default
    private final OffsetDateTime createdAt = OffsetDateTime.now();
    @Column("deleted_at")
    private final OffsetDateTime deletedAt;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Client client)) return false;
        return Objects.equals(id, client.id) && Objects.equals(idno, client.idno);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, idno);
    }
}
