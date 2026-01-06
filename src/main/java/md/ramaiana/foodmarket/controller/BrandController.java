package md.ramaiana.foodmarket.controller;

import md.ramaiana.foodmarket.controller.dto.products.BrandDto;
import md.ramaiana.foodmarket.model.Brand;
import md.ramaiana.foodmarket.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/brand")
public class BrandController {

    private final BrandService brandService;

    @Autowired
    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @GetMapping("/getAll")
    public ResponseEntity<?> getAll() {
        List<Brand> brands = brandService.getAllBrands();
        return ResponseEntity.ok(toDto(brands));
    }

    private List<BrandDto> toDto(List<Brand> brands) {
        return brands.stream()
                .map(b -> new BrandDto(b.getId(), b.getName()))
                .toList();
    }

}
