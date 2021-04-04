package md.ramaiana.foodmarket.service;

import lombok.extern.slf4j.Slf4j;
import md.ramaiana.foodmarket.dao.BrandDao;
import md.ramaiana.foodmarket.dao.GoodDao;
import md.ramaiana.foodmarket.dao.GoodGroupDao;
import md.ramaiana.foodmarket.model.Brand;
import md.ramaiana.foodmarket.model.Good;
import md.ramaiana.foodmarket.model.GoodGroup;
import md.ramaiana.foodmarket.model.GoodsReadResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * @author Dmitri Grosu (dmitri.grosu@gmail.com), 2/10/21
 */
@Service
@Slf4j
public class GoodService {

    private final GoodDao goodDao;
    private final GoodGroupDao goodGroupDao;
    private final DbfService dbfService;
    private final BrandDao brandDao;
    @Value("${dataFilePath}")
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
            return goodDao.getAllByGroupIdAndBrandIdAndNameIgnoreCaseContainingAndDeletedAtNull(groupId, brandId, name);
        } else if (groupId != null && brandId != null) {
            return goodDao.getAllByGroupIdAndBrandIdAndDeletedAtNull(groupId, brandId);
        } else if (groupId != null && name != null) {
            return goodDao.getAllByGroupIdAndNameIgnoreCaseContainingAndDeletedAtNull(groupId, name);
        } else if (brandId != null && name != null) {
            return goodDao.getAllByBrandIdAndNameIgnoreCaseContainingAndDeletedAtNull(brandId, name);
        } else if (groupId != null) {
            return goodDao.getAllByGroupIdAndDeletedAtNull(groupId);
        } else if (brandId != null) {
            return goodDao.getAllByBrandIdAndDeletedAtNull(brandId);
        } else if (name != null) {
            return goodDao.getAllByNameIgnoreCaseContainingAndDeletedAtNull(name);
        } else {
            return goodDao.getAllByGroupIdNullAndDeletedAtNull();
        }
    }

    public List<GoodGroup> findGroupsForGoodsList(List<Good> goods) {
        Map<Integer, GoodGroup> groupsMap = new HashMap<>();
        List<GoodGroup> topGroups = new ArrayList<>();
        for (Good good : goods) {
            Integer parentGroupId = good.getGroupId();
            addAllParentsToMap(parentGroupId, groupsMap);
        }
        for (GoodGroup group : groupsMap.values()) {
            if (group.getParentGroupId() == null) {
                topGroups.add(group);
            } else {
                groupsMap.get(group.getParentGroupId()).addChildIfAbsent(group);
            }
        }
        return topGroups;
    }

    public List<GoodGroup> getGroupsHierarchy(Integer parentGroupId) {
        List<GoodGroup> foundGroups;
        if (parentGroupId == null) {
            foundGroups = goodGroupDao.findByParentGroupIdNullAndDeletedAtNullOrderByName();
        } else {
            foundGroups = goodGroupDao.getAllByParentGroupIdAndDeletedAtNullOrderByName(parentGroupId);
        }
        for (GoodGroup foundGroup : foundGroups) {
            if (goodGroupDao.existsByParentGroupId(foundGroup.getId())) {
                List<GoodGroup> children = getGroupsHierarchy(foundGroup.getId());
                foundGroup.setChildGroups(children);
            }
        }
        return foundGroups;
    }

    @Scheduled(fixedDelayString = "${dataLoadingDelay}")
    public void loadGoods() {
        GoodsReadResult readResult;
        log.info("... data loading started");
        try {
            readResult = dbfService.readGoodsFromFile(filePath);
        } catch (FileNotFoundException ex) {
            log.warn(String.format("Could not start loading: file does not exists [%s]", filePath));
            return;
        }
        Map<String, String[]> erpCodes = readResult.getErpCodes();
        log.info(String.format("... read [%s] lines from DBF", erpCodes.size()));
        Map<String, Brand> updatedBrands = updateBrands(readResult.getBrands());
        log.info(String.format("... updated [%s] brands", updatedBrands.size()));
        Map<String, GoodGroup> updatedGroups = updateGroups(readResult);
        log.info(String.format("... updated [%s] groups", updatedGroups.size()));
        Map<String, Good> newGoods = readResult.getGoods();
        List<Integer> updatedGoodIds = new ArrayList<>();
        for (Good newGood : newGoods.values()) {
            String goodErp = newGood.getErpCode();
            String parentErp = erpCodes.get(goodErp)[0];
            if (parentErp != null) {
                GoodGroup parent = updatedGroups.get(parentErp);
                if (parent != null) {
                    newGood.setGroupId(parent.getId());
                }
            }
            String brandErp = erpCodes.get(goodErp)[1];
            if (brandErp != null) {
                Brand brand = updatedBrands.get(brandErp);
                newGood.setBrandId(brand.getId());
            }
            Good updatedGood = upsertGood(newGood);
            updatedGoodIds.add(updatedGood.getId());
        }
        log.info(String.format("... updated [%s] goods", updatedGoodIds.size()));
        int deletedCount = goodDao.setDeletedIfIdNotIn(updatedGoodIds);
        log.info(String.format("... mark deleted [%s] goods", deletedCount));
        deleteDataFile();
        log.info("... data loading finished");
    }

    private void deleteDataFile() {
        try {
            Files.deleteIfExists(Paths.get(filePath));
            log.info("... data file was deleted");
        } catch (IOException e) {
            log.error("Could not delete data file: " + e.getMessage());
        }
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
            newBrand.setCreatedAt(OffsetDateTime.now());
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
            if (foundGroup.updateIfChanged(newGroup)) {
                return goodGroupDao.save(foundGroup);
            } else {
                return foundGroup;
            }
        } else {
            newGroup.setCreatedAt(OffsetDateTime.now());
            return goodGroupDao.save(newGroup);
        }
    }

    @Transactional
    protected Good upsertGood(Good newGood) {
        Optional<Good> optionalGood = goodDao.findByErpCode(newGood.getErpCode());
        if (optionalGood.isPresent()) {
            Good foundGood = optionalGood.get();
            if (foundGood.updateIfChanged(newGood)) {
                return goodDao.save(foundGood);
            } else {
                return foundGood;
            }
        } else {
            newGood.setCreatedAt(OffsetDateTime.now());
            return goodDao.save(newGood);
        }
    }

    private void addAllParentsToMap(Integer childGroupId, Map<Integer, GoodGroup> parents) {
        if (parents == null) {
            parents = new HashMap<>();
        }
        Optional<GoodGroup> optionalGroup = goodGroupDao.findById(childGroupId);
        if (optionalGroup.isPresent()) {
            GoodGroup group = optionalGroup.get();
            parents.putIfAbsent(group.getId(), group);
            if (group.hasParent()) {
                addAllParentsToMap(group.getParentGroupId(), parents);
            }
        }
    }
}
