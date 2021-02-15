package md.ramaiana.foodmarket.service;

import md.ramaiana.foodmarket.dao.GoodDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class GoodServiceTest {
    @Mock
    private GoodDao goodDaoMock;

    @InjectMocks
    private GoodService goodService;

    @Test
    void test_getAllGoods() {
        //ARRANGE
        //ACT
        goodService.getAllGoods();
        //ASSERT
        verify(goodDaoMock, times(1)).getAllByDeletedAtNull();
    }
}
