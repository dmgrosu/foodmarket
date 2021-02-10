package md.ramaiana.foodmarket.service;

import md.ramaiana.foodmarket.dao.BrandDao;
import md.ramaiana.foodmarket.dao.GoodDao;
import md.ramaiana.foodmarket.dao.GoodGroupDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
