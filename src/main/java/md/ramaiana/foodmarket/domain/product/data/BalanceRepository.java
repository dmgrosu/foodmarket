package md.ramaiana.foodmarket.domain.product.data;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class BalanceRepository {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void updateBalances(final List<BalanceEntity> balances) {
        final int batchSize = 100;
        jdbcTemplate.execute("truncate table balances");
        jdbcTemplate.batchUpdate("insert into balances (storage_id, product_id, quantity) values (?,?,?)",
                balances,
                batchSize,
                (PreparedStatement ps, BalanceEntity balance) -> {
                    ps.setInt(1, balance.getStorage().getId());
                    ps.setInt(2, balance.getProduct().getId());
                    ps.setDouble(3, balance.getQuantity());
                });
    }

}
