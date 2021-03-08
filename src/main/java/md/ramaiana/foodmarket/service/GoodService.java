package md.ramaiana.foodmarket.service;

import md.ramaiana.foodmarket.dao.BrandDao;
import md.ramaiana.foodmarket.dao.GoodDao;
import md.ramaiana.foodmarket.dao.GoodGroupDao;
import md.ramaiana.foodmarket.model.Brand;
import md.ramaiana.foodmarket.model.Good;
import md.ramaiana.foodmarket.model.GoodGroup;
import md.ramaiana.foodmarket.model.GoodsReadResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;

/**
 * @author Dmitri Grosu (dmitri.grosu@gmail.com), 2/10/21
 */
@Service
public class GoodService {

    private final GoodDao goodDao;
    private final GoodGroupDao goodGroupDao;
    private final DbfService dbfService;
    private final BrandDao brandDao;
    @Value("${goodsFilePath}")
    private String filePath;

    @Autowired
    public GoodService(GoodDao goodDao,
                       GoodGroupDao goodGroupDao,
                       DbfService dbfService,
                       BrandDao brandDao) {
        this.goodDao = goodDao;
        this.goodGroupDao = goodGroupDao;
        this.dbfService = dbfService;
        this.brandDao = brandDao;
    }

    public List<Good> findGoodsFiltered(Integer groupId, Integer brandId, String name) {
        if (groupId != null && brandId != null && name != null) {
            return goodDao.getAllByGroupIdAndBrandIdAndNameLikeAndDeletedAtNull(groupId, brandId, name);
        } else if (groupId != null && brandId != null) {
            return goodDao.getAllByGroupIdAndBrandIdAndDeletedAtNull(groupId, brandId);
        } else if (groupId != null && name != null) {
            return goodDao.getAllByGroupIdAndNameAndDeletedAtNull(groupId, name);
        } else if (brandId != null && name != null) {
            return goodDao.getAllByBrandIdAndNameAndDeletedAtNull(brandId, name);
        } else if (groupId != null) {
            return goodDao.getAllByGroupIdAndDeletedAtNull(groupId);
        } else if (brandId != null) {
            return goodDao.getAllByBrandIdAndDeletedAtNull(brandId);
        } else if (name != null) {
            return goodDao.getAllByNameAndDeletedAtNull(name);
        } else {
            return goodDao.getAllByGroupIdNullAndDeletedAtNull();
        }
    }

    public List<GoodGroup> findGroupsFiltered(Integer parentGroupId) {
        if (parentGroupId != null) {
            return goodGroupDao.getAllByParentGroupIdAndDeletedAtNull(parentGroupId);
        } else {
            return goodGroupDao.getAllByParentGroupIdNullAndDeletedAtNull();
        }
    }

    public void loadGoods() {
        GoodsReadResult readResult = dbfService.readGoodsFromFile(filePath);
        Map<String, Brand> updatedBrands = updateBrands(readResult.getBrands());
        Map<String, GoodGroup> updatedGroups = updateGroups(readResult);
        Map<String, String[]> erpCodes = readResult.getErpCodes();
        Map<String, Good> newGoods = readResult.getGoods();
        List<Integer> updatedGoodIds = new ArrayList<>();
        for (Good newGood : newGoods.values()) {
            String goodErp = newGood.getErpCode();
            String parentErp = erpCodes.get(goodErp)[0];
            if (parentErp != null) {
                GoodGroup parent = updatedGroups.get(parentErp);
                newGood.setGroupId(parent.getId());
            }
            String brandErp = erpCodes.get(goodErp)[1];
            if (brandErp != null) {
                Brand brand = updatedBrands.get(brandErp);
                newGood.setBrandId(brand.getId());
            }
            Good updatedGood = upsertGood(newGood);
            updatedGoodIds.add(updatedGood.getId());
        }
        goodDao.setDeletedIfIdNotIn(updatedGoodIds);
    }

    protected Map<String, GoodGroup> updateGroups(GoodsReadResult readResult) {
        Map<String, GoodGroup> newGroups = readResult.getGroups();
        Map<String, String[]> erpCodes = readResult.getErpCodes();
        Map<String, GoodGroup> updatedGroups = new HashMap<>();
        for (GoodGroup group : newGroups.values()) {
            String parentErp = erpCodes.get(group.getErpCode())[0];
            GoodGroup savedGroup;
            if (parentErp == null) {
                savedGroup = upsertGroupByErpCode(group);
            } else {
                savedGroup = upsertGroupWithParent(group, newGroups.get(parentErp));
            }
            updatedGroups.put(savedGroup.getErpCode(), savedGroup);
        }
        return updatedGroups;
    }

    @Transactional
    protected Map<String, Brand> updateBrands(Map<String, Brand> newBrands) {
        Iterable<Brand> existingBrands = brandDao.findAll();
        Map<String, Brand> brandsMap = new HashMap<>();
        for (Brand existingBrand : existingBrands) {
            brandsMap.put(existingBrand.getErpCode(), existingBrand);
        }
        for (Brand newBrand : newBrands.values()) {
            String erpCode = newBrand.getErpCode();
            if (brandsMap.containsKey(erpCode)) {
                newBrand.setId(brandsMap.get(erpCode).getId());
            }
            brandsMap.put(newBrand.getErpCode(), newBrand);
        }
        for (Brand savedBrand : brandDao.saveAll(brandsMap.values())) {
            brandsMap.put(savedBrand.getErpCode(), savedBrand);
        }
        return brandsMap;
    }

    @Transactional
    protected GoodGroup upsertGroupWithParent(GoodGroup newGroup, GoodGroup parentGroup) {
        GoodGroup savedParent = upsertGroupByErpCode(parentGroup);
        newGroup.setParentGroupId(savedParent.getId());
        return upsertGroupByErpCode(newGroup);
    }

    @Transactional
    protected GoodGroup upsertGroupByErpCode(GoodGroup newGroup) {
        Optional<GoodGroup> optionalGroup = goodGroupDao.findByErpCode(newGroup.getErpCode());
        if (optionalGroup.isPresent()) {
            GoodGroup foundGroup = optionalGroup.get();
            if (foundGroup.getName().equals(newGroup.getName()) && !foundGroup.idDeleted()) {
                return foundGroup;
            } else {
                foundGroup.setName(newGroup.getName());
                foundGroup.setDeletedAt(null);
                foundGroup.setUpdatedAt(OffsetDateTime.now());
                return goodGroupDao.save(foundGroup);
            }
        } else {
            return goodGroupDao.save(newGroup);
        }
    }

    @Transactional
    protected Good upsertGood(Good newGood) {
        Optional<Good> optionalGood = goodDao.findByErpCode(newGood.getErpCode());
        if (optionalGood.isPresent()) {
            Good foundGood = optionalGood.get();
            if (foundGood.needsUpdate(newGood)) {
                foundGood.setName(newGood.getName());
                foundGood.setBarCode(newGood.getBarCode());
                foundGood.setPrice(newGood.getPrice());
                foundGood.setWeight(newGood.getWeight());
                foundGood.setBrandId(newGood.getBrandId());
                foundGood.setGroupId(newGood.getGroupId());
                foundGood.setInPackage(newGood.getInPackage());
                foundGood.setUnit(newGood.getUnit());
                foundGood.setDeletedAt(null);
                foundGood.setUpdatedAt(OffsetDateTime.now());
                return goodDao.save(foundGood);
            } else {
                return foundGood;
            }
        } else {
            return goodDao.save(newGood);
        }
    }
}
