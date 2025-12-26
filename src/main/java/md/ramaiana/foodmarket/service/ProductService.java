package md.ramaiana.foodmarket.service;

import lombok.extern.slf4j.Slf4j;
import md.ramaiana.foodmarket.dao.BrandDao;
import md.ramaiana.foodmarket.dao.ProductDao;
import md.ramaiana.foodmarket.dao.ProductGroupDao;
import md.ramaiana.foodmarket.model.Brand;
import md.ramaiana.foodmarket.model.Product;
import md.ramaiana.foodmarket.model.ProductGroup;
import md.ramaiana.foodmarket.model.ProductReadResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Service
@Slf4j
public class ProductService {

    private final ProductDao productDao;
    private final ProductGroupDao productGroupDao;
    private final DbfService dbfService;
    private final BrandDao brandDao;
    @Value("${dataFilePath}")
    private String filePath;

    @Autowired
    public ProductService(ProductDao productDao,
                          ProductGroupDao productGroupDao,
                          DbfService dbfService,
                          BrandDao brandDao) {
        this.productDao = productDao;
        this.productGroupDao = productGroupDao;
        this.dbfService = dbfService;
        this.brandDao = brandDao;
    }

    public List<Product> findProductsFiltered(Integer groupId, Integer brandId, String name) {
        if (groupId != null && brandId != null && name != null) {
            return productDao.getAllByGroupIdAndBrandIdAndNameIgnoreCaseContainingAndDeletedAtNull(groupId, brandId, name);
        } else if (groupId != null && brandId != null) {
            return productDao.getAllByGroupIdAndBrandIdAndDeletedAtNull(groupId, brandId);
        } else if (groupId != null && name != null) {
            return productDao.getAllByGroupIdAndNameIgnoreCaseContainingAndDeletedAtNull(groupId, name);
        } else if (brandId != null && name != null) {
            return productDao.getAllByBrandIdAndNameIgnoreCaseContainingAndDeletedAtNull(brandId, name);
        } else if (groupId != null) {
            return productDao.getAllByGroupIdAndDeletedAtNull(groupId);
        } else if (brandId != null) {
            return productDao.getAllByBrandIdAndDeletedAtNull(brandId);
        } else if (name != null) {
            return productDao.getAllByNameIgnoreCaseContainingAndDeletedAtNull(name);
        } else {
            return productDao.getAllByGroupIdNullAndDeletedAtNull();
        }
    }

    public List<ProductGroup> findProductsForProductsList(List<Product> products) {
        Map<Integer, ProductGroup> groupsMap = new HashMap<>();
        List<ProductGroup> topGroups = new ArrayList<>();
        for (Product product : products) {
            Integer parentGroupId = product.getGroupId();
            addAllParentsToMap(parentGroupId, groupsMap);
        }
        for (ProductGroup group : groupsMap.values()) {
            if (group.getParentGroupId() == null) {
                topGroups.add(group);
            } else {
                groupsMap.get(group.getParentGroupId()).addChildIfAbsent(group);
            }
        }
        return topGroups;
    }

    public List<ProductGroup> getGroupsHierarchy(Integer parentGroupId) {
        List<ProductGroup> foundGroups;
        if (parentGroupId == null) {
            foundGroups = productGroupDao.findByParentGroupIdNullAndDeletedAtNullOrderByName();
        } else {
            foundGroups = productGroupDao.getAllByParentGroupIdAndDeletedAtNullOrderByName(parentGroupId);
        }
        for (ProductGroup foundGroup : foundGroups) {
            if (productGroupDao.existsByParentGroupId(foundGroup.getId())) {
                List<ProductGroup> children = getGroupsHierarchy(foundGroup.getId());
                foundGroup.setChildGroups(children);
            }
        }
        return foundGroups;
    }

    @Scheduled(fixedDelayString = "${dataLoadingDelay}")
    public void loadProducts() {
        ProductReadResult readResult;
        log.info("... data loading started");
        try {
            readResult = dbfService.readGoodsFromFile(filePath);
        } catch (FileNotFoundException ex) {
            log.warn("Could not start loading: file does not exists {}", filePath);
            return;
        }
        Map<String, String[]> erpCodes = readResult.getErpCodes();
        log.info("... read {} lines from DBF", erpCodes.size());
        Map<String, Brand> updatedBrands = updateBrands(readResult.getBrands());
        log.info("... updated {} brands", updatedBrands.size());
        Map<String, ProductGroup> updatedGroups = updateGroups(readResult);
        log.info("... updated {} groups", updatedGroups.size());
        Map<String, Product> newProducts = readResult.getProducts();
        List<Integer> updatedProductIds = new ArrayList<>();
        for (Product newProduct : newProducts.values()) {
            String productErp = newProduct.getErpCode();
            String parentErp = erpCodes.get(productErp)[0];
            if (parentErp != null) {
                ProductGroup parent = updatedGroups.get(parentErp);
                if (parent != null) {
                    newProduct.setGroupId(parent.getId());
                }
            }
            String brandErp = erpCodes.get(productErp)[1];
            if (brandErp != null) {
                Brand brand = updatedBrands.get(brandErp);
                newProduct.setBrandId(brand.getId());
            }
            Product updatedProduct = upsertProduct(newProduct);
            updatedProductIds.add(updatedProduct.getId());
        }
        log.info("... updated {} goods", updatedProductIds.size());
        int deletedCount = productDao.setDeletedIfIdNotIn(updatedProductIds);
        log.info("... mark deleted {} goods", deletedCount);
        deleteDataFile();
        log.info("... data loading finished");
    }

    private void deleteDataFile() {
        try {
            Files.deleteIfExists(Paths.get(filePath));
            log.info("... data file was deleted");
        } catch (IOException e) {
            log.error("Could not delete data file: {}", e.getMessage());
        }
    }

    protected Map<String, ProductGroup> updateGroups(ProductReadResult readResult) {
        Map<String, ProductGroup> newGroups = readResult.getGroups();
        Map<String, String[]> erpCodes = readResult.getErpCodes();
        Map<String, ProductGroup> updatedGroups = new HashMap<>();
        for (ProductGroup group : newGroups.values()) {
            String parentErp = erpCodes.get(group.getErpCode())[0];
            ProductGroup savedGroup;
            if (parentErp == null) {
                savedGroup = upsertGroupByErpCode(group);
            } else {
                savedGroup = upsertGroupWithParent(group, newGroups.get(parentErp));
            }
            updatedGroups.put(savedGroup.getErpCode(), savedGroup);
        }
        return updatedGroups;
    }

    protected Map<String, Brand> updateBrands(Map<String, Brand> newBrands) {
        Map<String, Brand> existingBrands = new HashMap<>();
        for (Brand existingBrand : brandDao.findAll()) {
            existingBrands.put(existingBrand.getErpCode(), existingBrand);
        }
        for (Brand brand : newBrands.values()) {
            String erpCode = brand.getErpCode();
            Brand newBrand;
            if (existingBrands.containsKey(erpCode)) {
                newBrand = brand.withId(existingBrands.get(erpCode).getId());
            } else {
                newBrand = brand;
            }
            existingBrands.put(newBrand.getErpCode(), newBrand);
        }
        for (Brand savedBrand : brandDao.saveAll(existingBrands.values())) {
            existingBrands.put(savedBrand.getErpCode(), savedBrand);
        }
        return existingBrands;
    }

    @Transactional
    protected ProductGroup upsertGroupWithParent(ProductGroup newGroup, ProductGroup parentGroup) {
        ProductGroup savedParent = upsertGroupByErpCode(parentGroup);
        newGroup.setParentGroupId(savedParent.getId());
        return upsertGroupByErpCode(newGroup);
    }

    @Transactional
    protected ProductGroup upsertGroupByErpCode(ProductGroup newGroup) {
        Optional<ProductGroup> optionalGroup = productGroupDao.findByErpCode(newGroup.getErpCode());
        if (optionalGroup.isPresent()) {
            ProductGroup foundGroup = optionalGroup.get();
            return productGroupDao.save(foundGroup
                    .updateFrom(newGroup)
                    .withId(foundGroup.getId()));
        } else {
            return productGroupDao.save(newGroup);
        }
    }

    @Transactional
    protected Product upsertProduct(Product newProduct) {
        Optional<Product> optionalProduct = productDao.findByErpCode(newProduct.getErpCode());
        if (optionalProduct.isPresent()) {
            Product foundProduct = optionalProduct.get();
            return productDao.save(foundProduct.updateFrom(newProduct));
        } else {
            return productDao.save(newProduct);
        }
    }

    private void addAllParentsToMap(Integer childGroupId, Map<Integer, ProductGroup> parents) {
        if (parents == null) {
            parents = new HashMap<>();
        }
        Optional<ProductGroup> optionalGroup = productGroupDao.findById(childGroupId);
        if (optionalGroup.isPresent()) {
            ProductGroup group = optionalGroup.get();
            parents.putIfAbsent(group.getId(), group);
            if (group.hasParent()) {
                addAllParentsToMap(group.getParentGroupId(), parents);
            }
        }
    }

    public String getProductNameById(Integer goodId) {
        return productDao.getNameById(goodId);
    }

}
