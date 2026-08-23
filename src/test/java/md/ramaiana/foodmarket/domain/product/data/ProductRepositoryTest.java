package md.ramaiana.foodmarket.domain.product.data;

import md.ramaiana.foodmarket.domain.brand.data.BrandEntity;
import md.ramaiana.foodmarket.domain.brand.data.BrandRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProductRepositoryTest {

  @Autowired
  ProductRepository repository;

  @Autowired
  BrandRepository brandRepository;

  @Autowired
  ProductGroupRepository productGroupRepository;

  @AfterEach
  void cleanUp() {
    repository.deleteAll();
    productGroupRepository.deleteAll();
    brandRepository.deleteAll();
  }

  @Test
  void should_page_and_sort_products() {
    Integer brandId = brandRepository.save(new BrandEntity("Brand", "erp-brand")).getId();
    Integer groupId = productGroupRepository.save(new ProductGroupEntity("Group", "erp-group")).getId();

    repository.save(new ProductEntity("Cola", "pcs", 1f, "erp-cola", null, 1f, brandId, groupId));
    repository.save(new ProductEntity("Bread", "pcs", 1f, "erp-bread", null, 1f, brandId, groupId));
    repository.save(new ProductEntity("Apple", "pcs", 1f, "erp-apple", null, 1f, brandId, groupId));

    Page<ProductEntity> firstPage = repository.search(null, null, null,
        PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "name")));

    assertThat(firstPage.getTotalElements()).isEqualTo(3);
    assertThat(firstPage.getTotalPages()).isEqualTo(2);
    assertThat(firstPage.getContent()).extracting(ProductEntity::getName).containsExactly("Apple", "Bread");
  }

  @Test
  void should_filter_products_by_name_brand_and_group() {
    Integer brandOne = brandRepository.save(new BrandEntity("Brand One", "erp-brand-1")).getId();
    Integer brandTwo = brandRepository.save(new BrandEntity("Brand Two", "erp-brand-2")).getId();
    Integer groupOne = productGroupRepository.save(new ProductGroupEntity("Group One", "erp-group-1")).getId();
    Integer groupTwo = productGroupRepository.save(new ProductGroupEntity("Group Two", "erp-group-2")).getId();

    repository.save(new ProductEntity("Cola", "pcs", 1f, "erp-cola-2", null, 1f, brandOne, groupOne));
    repository.save(new ProductEntity("Coconut", "pcs", 1f, "erp-coconut", null, 1f, brandTwo, groupOne));
    repository.save(new ProductEntity("Bread", "pcs", 1f, "erp-bread-2", null, 1f, brandOne, groupTwo));

    Page<ProductEntity> byName = repository.search("co", null, null,
        PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name")));
    assertThat(byName.getTotalElements()).isEqualTo(2);

    Page<ProductEntity> byBrand = repository.search(null, brandOne, null,
        PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name")));
    assertThat(byBrand.getContent()).extracting(ProductEntity::getName).containsExactlyInAnyOrder("Cola", "Bread");

    Page<ProductEntity> byGroup = repository.search(null, null, groupTwo,
        PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name")));
    assertThat(byGroup.getContent()).extracting(ProductEntity::getName).containsExactly("Bread");
  }

  @Test
  void should_include_products_with_no_stock() {
    Integer brandId = brandRepository.save(new BrandEntity("Brand", "erp-brand-3")).getId();
    Integer groupId = productGroupRepository.save(new ProductGroupEntity("Group", "erp-group-3")).getId();

    repository.save(new ProductEntity("Zero Stock", "pcs", 1f, "erp-zero", null, 1f, brandId, groupId));

    Page<ProductEntity> page = repository.search(null, null, null,
        PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name")));

    assertThat(page.getContent()).extracting(ProductEntity::getName).contains("Zero Stock");
  }
}
