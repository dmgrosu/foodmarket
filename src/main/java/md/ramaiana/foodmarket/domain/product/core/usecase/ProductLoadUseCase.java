package md.ramaiana.foodmarket.domain.product.core.usecase;

import jakarta.annotation.Nullable;
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
            try {
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
                    if (brand != null) {
                        newProduct = newProduct.withBrandId(brand.getId());
                    }
                }
                ProductEntity updatedProduct = upsertProduct(newProduct);
                updatedProductIds.add(updatedProduct.getId());
            } catch (Exception e) {
                log.error("Error saving product {}: {}", newProduct.getErpCode(), e.getMessage());
            }
        }
        log.info("... updated {} products", updatedProductIds.size());
        //int deletedCount = productDao.setDeletedIfIdNotIn(updatedProductIds);
        //log.info("... mark deleted {} goods", deletedCount);
        log.info("... data loading finished");
    }

    /**
     * Persists every group the file refers to, keyed by ERP code.
     * <p>
     * A code can be referred to without being declared in the {@code <groups>} block - the ERP export
     * names only the groups it mirrors from products, and leaves the codes those actually hang under
     * undeclared. Such a code is still a real group: it is what {@code product.groupCode} points at, so
     * dropping it would leave every product in the catalogue ungrouped. It is created with the ERP code
     * standing in for the name until an export declares one, at which point the upsert picks it up.
     * <p>
     * A parent that cannot be resolved makes the group a root rather than discarding it.
     */
    private Map<String, ProductGroupEntity> updateGroups(ProductReadResult readResult) {
        Map<String, ProductGroupEntity> declaredGroups = readResult.getGroups();
        Map<String, String[]> erpCodes = readResult.getErpCodes();

        Set<String> allErpCodes = new LinkedHashSet<>(declaredGroups.keySet());
        for (String[] codes : erpCodes.values()) {
            if (hasText(codes[0])) {
                allErpCodes.add(codes[0]);
            }
        }

        Map<String, ProductGroupEntity> updatedGroups = new HashMap<>();
        for (String erpCode : allErpCodes) {
            upsertGroupTree(erpCode, declaredGroups, erpCodes, updatedGroups, new LinkedHashSet<>());
        }
        return updatedGroups;
    }

    /**
     * Saves {@code erpCode} after its parent, so the parent id is known by the time the child is
     * written. {@code branch} carries the codes already being resolved further up the same chain and
     * breaks a cycle in the ERP data by treating the group that closes it as a root.
     */
    @Nullable
    private ProductGroupEntity upsertGroupTree(String erpCode,
                                               Map<String, ProductGroupEntity> declaredGroups,
                                               Map<String, String[]> erpCodes,
                                               Map<String, ProductGroupEntity> updatedGroups,
                                               Set<String> branch) {
        ProductGroupEntity alreadySaved = updatedGroups.get(erpCode);
        if (alreadySaved != null) {
            return alreadySaved;
        }
        if (!branch.add(erpCode)) {
            log.warn("Group {} is its own ancestor in the ERP data - importing it as a root", erpCode);
            return null;
        }
        String[] codes = erpCodes.get(erpCode);
        String parentErp = codes == null ? null : codes[0];
        Integer parentGroupId = null;
        if (hasText(parentErp) && !parentErp.equals(erpCode)) {
            ProductGroupEntity parent =
                    upsertGroupTree(parentErp, declaredGroups, erpCodes, updatedGroups, branch);
            parentGroupId = parent == null ? null : parent.getId();
        }
        ProductGroupEntity declaredGroup = declaredGroups.get(erpCode);
        String name = declaredGroup == null ? erpCode : declaredGroup.getName();
        ProductGroupEntity savedGroup = upsertGroup(erpCode, name, parentGroupId);
        updatedGroups.put(erpCode, savedGroup);
        branch.remove(erpCode);
        return savedGroup;
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
                BrandEntity existingBrand = existingBrands.get(erpCode);
                newBrand = new BrandEntity(
                        existingBrand.getId(),
                        brand.getName(),
                        brand.getErpCode(),
                        existingBrand.getCreatedAt(),
                        existingBrand.getDeletedAt()
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

    private ProductGroupEntity upsertGroup(String erpCode, String name, Integer parentGroupId) {
        return productGroupRepository.findByErpCode(erpCode)
                .map(it -> productGroupRepository.save(it.withName(name).withParentGroupId(parentGroupId)))
                .orElseGet(() -> productGroupRepository.save(new ProductGroupEntity(name, parentGroupId, erpCode)));
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

}
