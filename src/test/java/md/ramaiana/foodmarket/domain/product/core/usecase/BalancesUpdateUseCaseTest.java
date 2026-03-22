package md.ramaiana.foodmarket.domain.product.core.usecase;

import md.ramaiana.foodmarket.domain.price.data.PriceEntity;
import md.ramaiana.foodmarket.domain.product.data.BalanceEntity;
import md.ramaiana.foodmarket.domain.product.data.BalanceRepository;
import md.ramaiana.foodmarket.domain.product.data.ProductEntity;
import md.ramaiana.foodmarket.domain.storage.core.usecase.StorageSearchUseCase;
import md.ramaiana.foodmarket.domain.storage.data.StorageEntity;
import md.ramaiana.foodmarket.shared.dataexchange.dto.ErpBalanceDto;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class BalancesUpdateUseCaseTest {

    @Mock
    BalanceRepository balanceRepository;
    @Mock
    ProductFindByErpCodeUseCase productSearch;
    @Mock
    StorageSearchUseCase storageSearch;
    @InjectMocks
    BalancesUpdateUseCase useCase;
    @Captor
    ArgumentCaptor<List<BalanceEntity>> captor;

    @Test
    void execute_should_map_balances_and_update_repository() {
        // Arrange
        ErpBalanceDto balanceDto1 = new ErpBalanceDto();
        balanceDto1.setStorageCode("STORAGE1");
        balanceDto1.setProductCode("erp1");
        balanceDto1.setQuantity(100.0f);
        ErpBalanceDto balanceDto2 = new ErpBalanceDto();
        balanceDto2.setStorageCode("STORAGE2");
        balanceDto2.setProductCode("erp2");
        balanceDto2.setQuantity(50.0f);
        when(storageSearch.findByErpCode("STORAGE1"))
                .thenReturn(new StorageEntity(1, "STORAGE1", "Storage 1"));
        when(storageSearch.findByErpCode("STORAGE2"))
                .thenReturn(new StorageEntity(2, "STORAGE2", "Storage 2"));
        when(productSearch.execute("erp1"))
                .thenReturn(getProductEntity(1, "Product 1", "erp1", 11f));
        when(productSearch.execute("erp2"))
                .thenReturn(getProductEntity(2, "Product 2", "erp2", 22f));

        // Act
        useCase.execute(Arrays.asList(balanceDto1, balanceDto2));

        // Assert
        verify(balanceRepository).updateBalances(captor.capture());
        List<BalanceEntity> capturedBalances = captor.getValue();
        assertThat(capturedBalances).hasSize(2);
        BalanceEntity balance1 = capturedBalances.getFirst();
        assertThat(balance1.getStorage().getId()).isEqualTo(1);
        assertThat(balance1.getProduct().getId()).isEqualTo(1);
        assertThat(balance1.getQuantity()).isEqualTo(100.0f);
        BalanceEntity balance2 = capturedBalances.get(1);
        assertThat(balance2.getStorage().getId()).isEqualTo(2);
        assertThat(balance2.getProduct().getId()).isEqualTo(2);
        assertThat(balance2.getQuantity()).isEqualTo(50.0f);
    }


    @Test
    void execute_should_skip_invalid_balances() {
        // Arrange
        ErpBalanceDto validBalanceDto = new ErpBalanceDto();
        validBalanceDto.setStorageCode("STORAGE_VALID");
        validBalanceDto.setProductCode("PRODUCT_VALID");
        validBalanceDto.setQuantity(10.0f);
        ErpBalanceDto invalidBalanceDto = new ErpBalanceDto();
        invalidBalanceDto.setStorageCode("STORAGE_INVALID");
        invalidBalanceDto.setProductCode("PRODUCT_INVALID");
        invalidBalanceDto.setQuantity(25.0f);
        when(storageSearch.findByErpCode("STORAGE_VALID"))
                .thenReturn(new StorageEntity(1, "STORAGE_VALID", "Valid Storage"));
        when(productSearch.execute("PRODUCT_VALID"))
                .thenReturn(getProductEntity(1, "Product 2", "erp2", 11f));
        when(storageSearch.findByErpCode("STORAGE_INVALID"))
                .thenThrow(new RuntimeException("Invalid storage"));

        // Act
        useCase.execute(Arrays.asList(validBalanceDto, invalidBalanceDto));

        // Assert
        verify(balanceRepository).updateBalances(captor.capture());
        List<BalanceEntity> capturedBalances = captor.getValue();
        assertThat(capturedBalances).hasSize(1);
        BalanceEntity validBalance = capturedBalances.getFirst();
        assertThat(validBalance.getStorage().getId()).isEqualTo(1);
        assertThat(validBalance.getProduct().getId()).isEqualTo(1);
        assertThat(validBalance.getQuantity()).isEqualTo(10.0f);
    }

    @Test
    void execute_should_do_nothing_when_no_balances_provided() {
        // Act
        useCase.execute(Collections.emptyList());

        // Assert
        verify(balanceRepository, never()).updateBalances(any());
    }

    private ProductEntity getProductEntity(Integer id, String name, String erp1, Float price) {
        return new ProductEntity(id, name, "unit", 1f, erp1,
                "barCode", 12.34f, null, null,
                Instant.now(), null, null, Set.of(
                        new PriceEntity(price)
        ));
    }

}