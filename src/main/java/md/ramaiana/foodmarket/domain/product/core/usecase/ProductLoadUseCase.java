package md.ramaiana.foodmarket.domain.product.core.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import md.ramaiana.foodmarket.domain.brand.data.BrandEntity;
import md.ramaiana.foodmarket.domain.brand.data.BrandRepository;
import md.ramaiana.foodmarket.domain.product.data.ProductEntity;
import md.ramaiana.foodmarket.domain.product.data.ProductGroupEntity;
import md.ramaiana.foodmarket.domain.product.data.ProductGroupRepository;
import md.ramaiana.foodmarket.domain.product.data.ProductRepository;
import md.ramaiana.foodmarket.shared.dataexchange.core.data.ProductReadResult;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.springframework.util.StringUtils.hasText;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class ProductLoadUseCase {

    private final ProductRepository productRepository;
    private final ProductGroupRepository productGroupRepository;
    private final BrandRepository brandRepository;

    @Transactional
    public void execute(ProductReadResult readResult) {
        log.info("... data loading started");
        Map<String, String[]> erpCodes = readResult.getErpCodes();
        log.info("... read {} lines", erpCodes.size());
        Map<String, BrandEntity> updatedBrands = updateBrands(readResult.getBrands());
        log.info("... updated {} brands", updatedBrands.size());
        Map<String, ProductGroupEntity> updatedGroups = updateGroups(readResult);
        log.info("... updated {} groups", updatedGroups.size());
        Map<String, ProductEntity> newProducts = readResult.getProducts();
        List<Integer> updatedProductIds = new ArrayList<>();
        for (ProductEntity newProduct : newProducts.values()) {
            String productErp = newProduct.getErpCode();
            String parentErp = erpCodes.get(productErp)[0];
            if (hasText(parentErp)) {
                ProductGroupEntity parent = updatedGroups.get(parentErp);
                if (parent != null) {
                    newProduct = newProduct.withGroupId(parent.getId());
                }
            }
            String brandErp = erpCodes.get(productErp)[1];
            if (hasText(brandErp)) {
                BrandEntity brand = updatedBrands.get(brandErp);
                newProduct = newProduct.withBrandId(brand.getId());
            }
            ProductEntity updatedProduct = upsertProduct(newProduct);
            updatedProductIds.add(updatedProduct.getId());
        }
        log.info("... updated {} goods", updatedProductIds.size());
        //int deletedCount = productDao.setDeletedIfIdNotIn(updatedProductIds);
        //log.info("... mark deleted {} goods", deletedCount);
        log.info("... data loading finished");
    }

    private Map<String, ProductGroupEntity> updateGroups(ProductReadResult readResult) {
        Map<String, ProductGroupEntity> newGroups = readResult.getGroups();
        Map<String, String[]> erpCodes = readResult.getErpCodes();
        Map<String, ProductGroupEntity> updatedGroups = new HashMap<>();
        for (ProductGroupEntity group : newGroups.values()) {
            String parentErp = erpCodes.get(group.getErpCode())[0];
            ProductGroupEntity savedGroup;
            if (hasText(parentErp)) {
                savedGroup = upsertGroupWithParent(group, newGroups.get(parentErp));
            } else {
                savedGroup = upsertGroupByErpCode(group);
            }
            updatedGroups.put(savedGroup.getErpCode(), savedGroup);
        }
        return updatedGroups;
    }

    private Map<String, BrandEntity> updateBrands(Map<String, BrandEntity> newBrands) {
        Map<String, BrandEntity> existingBrands = new HashMap<>();
        for (BrandEntity existingBrand : brandRepository.findAll()) {
            existingBrands.put(existingBrand.getErpCode(), existingBrand);
        }
        for (BrandEntity brand : newBrands.values()) {
            String erpCode = brand.getErpCode();
            BrandEntity newBrand;
            if (existingBrands.containsKey(erpCode)) {
                newBrand = new BrandEntity(
                        existingBrands.get(erpCode).getId(),
                        brand.getName(),
                        brand.getErpCode()
                );
            } else {
                newBrand = brand;
            }
            existingBrands.put(newBrand.getErpCode(), newBrand);
        }
        for (BrandEntity savedBrand : brandRepository.saveAll(existingBrands.values())) {
            existingBrands.put(savedBrand.getErpCode(), savedBrand);
        }
        return existingBrands;
    }

    private ProductGroupEntity upsertGroupWithParent(ProductGroupEntity newGroup, ProductGroupEntity parentGroup) {
        ProductGroupEntity savedParent = upsertGroupByErpCode(parentGroup);
        return upsertGroupByErpCode(newGroup.withParentGroupId(savedParent.getId()));
    }

    private ProductGroupEntity upsertGroupByErpCode(ProductGroupEntity newGroup) {
        return productGroupRepository.findByErpCode(newGroup.getErpCode())
                .map(it -> productGroupRepository.save(it.withName(newGroup.getName())))
                .orElseGet(() -> productGroupRepository.save(newGroup));
    }

    private ProductEntity upsertProduct(ProductEntity newProduct) {
        Optional<ProductEntity> optionalProduct = productRepository.findByErpCode(newProduct.getErpCode());
        if (optionalProduct.isPresent()) {
            ProductEntity foundProduct = optionalProduct.get();
            return productRepository.save(foundProduct.updateFrom(newProduct));
        } else {
            return productRepository.save(newProduct);
        }
    }

    private void addAllParentsToMap(Integer childGroupId, Map<Integer, ProductGroupEntity> parents) {
        if (parents == null) {
            parents = new HashMap<>();
        }
        Optional<ProductGroupEntity> optionalGroup = productGroupRepository.findById(childGroupId);
        if (optionalGroup.isPresent()) {
            ProductGroupEntity group = optionalGroup.get();
            parents.putIfAbsent(group.getId(), group);
            if (group.hasParent()) {
                addAllParentsToMap(group.getParentGroupId(), parents);
            }
        }
    }


}
