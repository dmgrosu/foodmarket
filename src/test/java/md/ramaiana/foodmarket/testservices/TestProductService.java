package md.ramaiana.foodmarket.testservices;

import md.ramaiana.foodmarket.domain.product.data.ProductEntity;
import md.ramaiana.foodmarket.domain.product.data.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TestProductService {

  private final ProductRepository productRepository;
  private int counter = 0;

  public TestProductService(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  @Transactional
  public ProductEntity create(Integer brandId, Integer groupId) {
    counter++;
    var product = new ProductEntity();
    product.setName("Test Product " + counter);
    product.setPrice(10.0f * counter);
    product.setWeight(1.5f);
    product.setUnit("kg");
    product.setBrandId(brandId);
    product.setGroupId(groupId);
    return productRepository.save(product);
  }

  @Transactional
  public ProductEntity create(String name, Float price, Float weight, Integer brandId, Integer groupId) {
    var product = new ProductEntity();
    product.setName(name);
    product.setPrice(price);
    product.setWeight(weight);
    product.setUnit("kg");
    product.setBrandId(brandId);
    product.setGroupId(groupId);
    return productRepository.save(product);
  }
}
