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

    /**
     * ERP codes are numeric, so this prefix cannot collide with one, and it is deterministic so the
     * folder is reused rather than duplicated on the next import.
     */
    private static final String DERIVED_FOLDER_ERP_PREFIX = "derived:";
    /**
     * A folder holding a single group is worse than no folder: it adds a click and shows nothing the
     * level above did not already show.
     */
    private static final int MIN_GROUPS_PER_DERIVED_FOLDER = 2;

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
        log.info("... filed them under {} derived folders", bucketRootsIntoDerivedFolders(updatedGroups));
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
     * dropping it would leave every product in the catalogue ungrouped. Its name is derived from the
     * products filed under it (see {@link ProductGroupNaming}), falling back to the ERP code; either
     * way a name the export declares later wins, because the upsert reads the declared one first.
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

        Map<String, List<String>> productNamesByGroup = productNamesByGroup(readResult);

        Map<String, ProductGroupEntity> updatedGroups = new HashMap<>();
        for (String erpCode : allErpCodes) {
            upsertGroupTree(erpCode, declaredGroups, erpCodes, productNamesByGroup, updatedGroups,
                    new LinkedHashSet<>());
        }
        return updatedGroups;
    }

    /**
     * Gives the groups the ERP left at the top level a parent, so the catalogue is a tree rather
     * than several hundred loose entries.
     * <p>
     * The only hierarchy available is the one in the names: products are named category-first, so
     * groups whose derived names start with the same word belong together — ПОСУДА ЧАШКА, ПОСУДА
     * СТАКАН and ПОСУДА ТАРЕЛКА under ПОСУДА. A group the ERP did place stays where it put it, and
     * a declared parent overwrites this on the next import.
     *
     * @return how many folders the roots ended up under.
     */
    private int bucketRootsIntoDerivedFolders(Map<String, ProductGroupEntity> savedGroups) {
        Map<String, List<ProductGroupEntity>> rootsByFirstWord = new LinkedHashMap<>();
        for (ProductGroupEntity group : savedGroups.values()) {
            if (group.hasParent()) {
                continue;
            }
            String firstWord = firstWordOf(group.getName());
            if (firstWord != null) {
                rootsByFirstWord.computeIfAbsent(firstWord, word -> new ArrayList<>()).add(group);
            }
        }

        int folders = 0;
        for (Map.Entry<String, List<ProductGroupEntity>> byWord : rootsByFirstWord.entrySet()) {
            List<ProductGroupEntity> roots = byWord.getValue();
            if (roots.size() < MIN_GROUPS_PER_DERIVED_FOLDER) {
                continue;
            }
            ProductGroupEntity folder = upsertGroup(
                    DERIVED_FOLDER_ERP_PREFIX + byWord.getKey(), byWord.getKey(), null);
            for (ProductGroupEntity root : roots) {
                productGroupRepository.save(root.withParentGroupId(folder.getId()));
            }
            folders++;
        }
        return folders;
    }

    @Nullable
    private String firstWordOf(String name) {
        String[] words = name.trim().split("\\s+");
        return words.length == 0 || words[0].isBlank() ? null : words[0];
    }

    /**
     * The names of the products filed under each group code, which is what an undeclared group's
     * name is derived from.
     */
    private Map<String, List<String>> productNamesByGroup(ProductReadResult readResult) {
        Map<String, String[]> erpCodes = readResult.getErpCodes();
        Map<String, List<String>> namesByGroup = new HashMap<>();
        for (Map.Entry<String, ProductEntity> product : readResult.getProducts().entrySet()) {
            String[] codes = erpCodes.get(product.getKey());
            String groupErpCode = codes == null ? null : codes[0];
            if (hasText(groupErpCode)) {
                namesByGroup.computeIfAbsent(groupErpCode, key -> new ArrayList<>())
                        .add(product.getValue().getName());
            }
        }
        return namesByGroup;
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
                                               Map<String, List<String>> productNamesByGroup,
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
            ProductGroupEntity parent = upsertGroupTree(parentErp, declaredGroups, erpCodes,
                    productNamesByGroup, updatedGroups, branch);
            parentGroupId = parent == null ? null : parent.getId();
        }
        String name = nameFor(erpCode, declaredGroups.get(erpCode), productNamesByGroup);
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

    /**
     * A declared name first, then one derived from the group's products, and the ERP code only when
     * neither is available.
     */
    private String nameFor(String erpCode, @Nullable ProductGroupEntity declaredGroup,
                           Map<String, List<String>> productNamesByGroup) {
        if (declaredGroup != null) {
            return declaredGroup.getName();
        }
        String derived = ProductGroupNaming.deriveFrom(
                productNamesByGroup.getOrDefault(erpCode, List.of()));
        return derived == null ? erpCode : derived;
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
