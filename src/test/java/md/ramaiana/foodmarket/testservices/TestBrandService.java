package md.ramaiana.foodmarket.testservices;

import md.ramaiana.foodmarket.domain.brand.data.BrandEntity;
import md.ramaiana.foodmarket.domain.brand.data.BrandRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TestBrandService {

  private final BrandRepository brandRepository;
  private int counter = 0;

  public TestBrandService(BrandRepository brandRepository) {
    this.brandRepository = brandRepository;
  }

  @Transactional
  public BrandEntity create() {
    counter++;
    return create("Test Brand " + counter, "ERP" + counter);
  }

  @Transactional
  public BrandEntity create(String name, String erpCode) {
    var brand = new BrandEntity();
    brand.setName(name);
    brand.setErpCode(erpCode);
    return brandRepository.save(brand);
  }
}
