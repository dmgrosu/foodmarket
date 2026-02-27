package md.ramaiana.foodmarket.testservices;

import md.ramaiana.foodmarket.domain.product.data.ProductGroupEntity;
import md.ramaiana.foodmarket.domain.product.data.ProductGroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TestProductGroupService {

  private final ProductGroupRepository productGroupRepository;
  private int counter = 0;

  public TestProductGroupService(ProductGroupRepository productGroupRepository) {
    this.productGroupRepository = productGroupRepository;
  }

  @Transactional
  public ProductGroupEntity create() {
    counter++;
    return create("Test Group " + counter, null);
  }

  @Transactional
  public ProductGroupEntity create(Integer parentGroupId) {
    counter++;
    return create("Test Group " + counter, parentGroupId);
  }

  @Transactional
  public ProductGroupEntity create(String name, Integer parentGroupId) {
    var group = new ProductGroupEntity();
    group.setName(name);
    group.setParentGroupId(parentGroupId);
    return productGroupRepository.save(group);
  }
}
