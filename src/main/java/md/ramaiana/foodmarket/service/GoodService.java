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

    public List<Good> getAllGoods() {
        return goodDao.getAllByDeletedAtNull();
    }

    public List<Good> findGoodsFiltered(Integer groupId, Integer brandId, String name) {
        if (groupId != null && brandId != null && name != null) {
            return goodDao.getAllByGroupIdAndBrandIdAndNameLike(groupId, brandId, name); // tested
        } else if (groupId != null && brandId != null) {
            return goodDao.getAllByGroupIdAndBrandId(groupId, brandId); // tested
        } else if (groupId != null && name != null) {
            return goodDao.getAllByGroupIdAndName(groupId, name); // tested
        } else if (brandId != null && name != null) {
            return goodDao.getAllByBrandIdAndName(brandId, name); // tested
        } else if (groupId != null) {
            return goodDao.getAllByGroupId(groupId); // tested
        } else if (brandId != null) {
            return goodDao.getAllByBrandId(brandId); // tested
        } else if (name != null) {
            return goodDao.getAllByName(name); // tested
        } else {
//            List<GoodGroup> groups = goodGroupDao.getAllByParentGroupIdNullAndDeletedAtNull();
//            List<Good> goods = goodDao.getAllByGroupIdNullAndDeletedAtNull();
//            groups.forEach(groupList::addGroup);
//            goods.forEach(groupList::addGood);
            return null;
        }

    }
}
