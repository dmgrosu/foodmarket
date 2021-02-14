package md.ramaiana.foodmarket.controller;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import md.ramaiana.foodmarket.model.Brand;
import md.ramaiana.foodmarket.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import md.ramaiana.foodmarket.proto.Goods;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/brand")
public class BrandController {

    private final BrandService brandService;
    private final JsonFormat.Printer printer;

    @Autowired
    public BrandController(BrandService brandService) {
        this.brandService = brandService;
        this.printer = JsonFormat.printer().omittingInsignificantWhitespace();
    }

    @GetMapping("/getAll")
    public ResponseEntity<?> getAll() throws InvalidProtocolBufferException {
        List<Brand> brands = brandService.getAllBrands();
        return ResponseEntity.ok(printer.print(buildProtoFromDomain(brands)));
    }

    private Goods.BrandListResponse buildProtoFromDomain(List<Brand> brands) {
        List<Goods.Brand> protoBrands = new ArrayList<>();
        for (Brand brand : brands) {
            protoBrands.add(Goods.Brand.newBuilder()
            .setId(brand.getId())
            .setName(brand.getName())
            .build());
        }
        return Goods.BrandListResponse.newBuilder()
                .addAllBrands(protoBrands)
                .build();
    }

}
