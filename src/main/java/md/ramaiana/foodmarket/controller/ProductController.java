package md.ramaiana.foodmarket.controller;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import md.ramaiana.foodmarket.model.Product;
import md.ramaiana.foodmarket.model.ProductGroup;
import md.ramaiana.foodmarket.proto.Products;
import md.ramaiana.foodmarket.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/good")
public class ProductController {

    private final ProductService productService;
    private final JsonFormat.Printer printer;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
        this.printer = JsonFormat.printer().omittingInsignificantWhitespace();
    }

    @GetMapping("/listGroups")
    public ResponseEntity<?> getGroupsByParent(@RequestParam(value = "parentGroupId", required = false) Integer parentGroupId) throws InvalidProtocolBufferException {
        List<ProductGroup> groups = productService.getGroupsHierarchy(parentGroupId);
        return ResponseEntity.ok(printer.print(buildGoodsListResponse(Collections.emptyList(), groups)));
    }

    @GetMapping("/listGoods")
    public ResponseEntity<?> getGoodsByGroup(@RequestParam("groupId") Integer groupId,
                                             @RequestParam(value = "brandId", required = false) Integer brandId,
                                             @RequestParam(value = "name", required = false) String nameLike) throws InvalidProtocolBufferException {
        List<Product> products = productService.findProductsFiltered(groupId, brandId, nameLike);
        return ResponseEntity.ok(printer.print(buildGoodsListResponse(products, Collections.emptyList())));
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchGoods(@RequestParam(value = "groupId", required = false) Integer groupId,
                                         @RequestParam(value = "brandId", required = false) Integer brandId,
                                         @RequestParam(value = "name", required = false) String nameLike) throws InvalidProtocolBufferException {
        List<Product> products = productService.findProductsFiltered(groupId, brandId, nameLike);
        List<ProductGroup> groups = productService.findProductsForProductsList(products);
        return ResponseEntity.ok(printer.print(buildGoodsListResponse(products, groups)));
    }

    private Products.GoodsListResponse buildGoodsListResponse(List<Product> products, List<ProductGroup> groups) {
        return Products.GoodsListResponse.newBuilder()
                .addAllProducts(mapGoodsToProto(products))
                .addAllGroups(mapGroupsToProto(groups))
                .build();
    }

    private List<Products.Product> mapGoodsToProto(List<Product> products) {
        return products.stream().map(good -> Products.Product.newBuilder()
                .setId(good.getId())
                .setName(good.getName())
                .setBrandId(good.getBrandId())
                .setGroupId(good.getGroupId())
                .setUnit(good.getUnit())
                .setPackage(good.getInPackage())
                .setBarCode(good.getBarCode())
                .setWeight(good.getWeight())
                .setPrice(good.getPrice())
                .build()).collect(Collectors.toList());
    }

    private List<Products.Group> mapGroupsToProto(List<ProductGroup> groups) {
        List<Products.Group> protoGroups = new ArrayList<>();
        for (ProductGroup group : groups) {
            List<Products.Group> childrenProto = null;
            if (group.hasChildren()) {
                childrenProto = mapGroupsToProto(group.getChildGroups());
            }
            Products.Group protoGroup = Products.Group.newBuilder()
                    .setId(group.getId())
                    .setName(group.getName())
                    .addAllGroups(childrenProto != null ? childrenProto : Collections.emptyList())
                    .build();
            protoGroups.add(protoGroup);
        }
        return protoGroups;
    }
}
