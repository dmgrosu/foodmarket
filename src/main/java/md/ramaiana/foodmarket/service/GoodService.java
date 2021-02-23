package md.ramaiana.foodmarket.service;

import md.ramaiana.foodmarket.dao.BrandDao;
import md.ramaiana.foodmarket.dao.GoodDao;
import md.ramaiana.foodmarket.dao.GoodGroupDao;
import md.ramaiana.foodmarket.model.Good;
import md.ramaiana.foodmarket.model.GoodGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Dmitri Grosu (dmitri.grosu@gmail.com), 2/10/21
 */
@Service
public class GoodService {

    private final GoodDao goodDao;
    private final GoodGroupDao goodGroupDao;
    private final BrandDao brandDao;

    @Autowired
    public GoodService(GoodDao goodDao,
                       GoodGroupDao goodGroupDao,
                       BrandDao brandDao) {
        this.goodDao = goodDao;
        this.goodGroupDao = goodGroupDao;
        this.brandDao = brandDao;
    }

    public List<Good> findGoodsFiltered(Integer groupId, Integer brandId, String name) {
        if (groupId != null && brandId != null && name != null) {
            return goodDao.getAllByGroupIdAndBrandIdAndNameLikeAndDeletedAtNull(groupId, brandId, name); // tested
        } else if (groupId != null && brandId != null) {
            return goodDao.getAllByGroupIdAndBrandIdAndDeletedAtNull(groupId, brandId); // tested
        } else if (groupId != null && name != null) {
            return goodDao.getAllByGroupIdAndNameAndDeletedAtNull(groupId, name); // tested
        } else if (brandId != null && name != null) {
            return goodDao.getAllByBrandIdAndNameAndDeletedAtNull(brandId, name); // tested
        } else if (groupId != null) {
            return goodDao.getAllByGroupIdAndDeletedAtNull(groupId); // tested
        } else if (brandId != null) {
            return goodDao.getAllByBrandIdAndDeletedAtNull(brandId); // tested
        } else if (name != null) {
            return goodDao.getAllByNameAndDeletedAtNull(name); // tested
        } else {
            return goodDao.getAllByGroupIdNullAndDeletedAtNull(); // tested
        }
    }

    public List<GoodGroup> findGroupsFiltered(Integer parentGroupId) {
        if (parentGroupId != null){
            return goodGroupDao.getAllByParentGroupIdAndDeletedAtNull(parentGroupId); // tested
        } else {
            return goodGroupDao.getAllByParentGroupIdNullAndDeletedAtNull(); // tested
        }
    }
}
