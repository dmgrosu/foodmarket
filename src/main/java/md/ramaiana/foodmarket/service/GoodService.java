package md.ramaiana.foodmarket.service;

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

    @Autowired
    public GoodService(GoodDao goodDao,
                       GoodGroupDao goodGroupDao) {
        this.goodDao = goodDao;
        this.goodGroupDao = goodGroupDao;
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
        if (parentGroupId != null){
            return goodGroupDao.getAllByParentGroupIdAndDeletedAtNull(parentGroupId);
        } else {
            return goodGroupDao.getAllByParentGroupIdNullAndDeletedAtNull();
        }
    }
}
