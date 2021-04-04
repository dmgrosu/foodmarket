package md.ramaiana.foodmarket.service;

import md.ramaiana.foodmarket.dao.BrandDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
public class BrandServiceTest {
    @Mock
    private BrandDao brandDaoMock;
    @InjectMocks
    private BrandService brandService;

    @Test
    void test_getAllBrands() {
        //ACT
        brandService.getAllBrands();
        //ASSERT
        verify(brandDaoMock, times(1)).getAllByDeletedAtNullOrderByName();
    }
}
