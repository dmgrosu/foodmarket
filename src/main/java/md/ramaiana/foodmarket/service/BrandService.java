package md.ramaiana.foodmarket.service;


import md.ramaiana.foodmarket.dao.BrandDao;
import md.ramaiana.foodmarket.model.Brand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Grosu Kirill (grosukirill009@gmail.com), 2/14/2021
 */

@Service
public class BrandService {

    private final BrandDao brandDao;

    @Autowired
    public BrandService(BrandDao brandDao) {
        this.brandDao = brandDao;
    }

    public List<Brand> getAllBrands() {
        return brandDao.getAllByDeletedAtNull();
    }
}
