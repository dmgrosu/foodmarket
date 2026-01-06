package md.ramaiana.foodmarket.controller;

import md.ramaiana.foodmarket.controller.dto.products.GroupDto;
import md.ramaiana.foodmarket.controller.dto.products.ProductDto;
import md.ramaiana.foodmarket.controller.dto.products.ProductListResponseDto;
import md.ramaiana.foodmarket.model.Product;
import md.ramaiana.foodmarket.model.ProductGroup;
import md.ramaiana.foodmarket.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;

@RestController
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/listGroups")
    public ResponseEntity<?> getGroupsByParent(@RequestParam(value = "parentGroupId", required = false) Integer parentGroupId) {
        List<ProductGroup> groups = productService.getGroupsHierarchy(parentGroupId);
        return ResponseEntity.ok(buildProductsListResponse(emptyList(), groups));
    }

    @GetMapping("/listProducts")
    public ResponseEntity<?> getProductsByGroup(@RequestParam("groupId") Integer groupId,
                                                @RequestParam(value = "brandId", required = false) Integer brandId,
                                                @RequestParam(value = "name", required = false) String nameLike) {
        List<Product> products = productService.findProductsFiltered(groupId, brandId, nameLike);
        return ResponseEntity.ok(buildProductsListResponse(products, emptyList()));
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchProducts(@RequestParam(value = "groupId", required = false) Integer groupId,
                                            @RequestParam(value = "brandId", required = false) Integer brandId,
                                            @RequestParam(value = "name", required = false) String nameLike) {
        List<Product> products = productService.findProductsFiltered(groupId, brandId, nameLike);
        List<ProductGroup> groups = productService.findProductsForProductsList(products);
        return ResponseEntity.ok(buildProductsListResponse(products, groups));
    }

    private ProductListResponseDto buildProductsListResponse(List<Product> products, List<ProductGroup> groups) {
        return new ProductListResponseDto(
                products.stream().map(this::toDto).toList(),
                toDto(groups)
        );
    }

    private ProductDto toDto(Product product) {
        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getGroupId(),
                product.getBrandId(),
                product.getInPackage(),
                product.getBarCode(),
                product.getUnit(),
                product.getWeight());
    }

    private List<GroupDto> toDto(List<ProductGroup> groups) {
        List<GroupDto> groupDtos = new ArrayList<>();
        for (ProductGroup group : groups) {
            List<GroupDto> children = group.hasChildren() ?
                    toDto(group.getChildGroups()) :
                    List.of();
            groupDtos.add(new GroupDto(
                    group.getId(),
                    group.getName(),
                    children,
                    List.of()
            ));
        }
        return groupDtos;
    }
}
