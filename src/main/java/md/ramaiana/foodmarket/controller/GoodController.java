package md.ramaiana.foodmarket.controller;


import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import md.ramaiana.foodmarket.model.Good;
import md.ramaiana.foodmarket.model.GoodGroup;
import md.ramaiana.foodmarket.proto.Goods;
import md.ramaiana.foodmarket.service.GoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/good")
public class GoodController {

    private final GoodService goodService;
    private final JsonFormat.Printer printer;

    @Autowired
    public GoodController(GoodService goodService) {
        this.goodService = goodService;
        this.printer = JsonFormat.printer().omittingInsignificantWhitespace();
    }

    @GetMapping("/getAll")
    public ResponseEntity<?> getAllGoods(@RequestParam(value = "group_id", required = false) Integer groupId,
                                         @RequestParam(value = "brand_id", required = false) Integer brandId,
                                         @RequestParam(value = "name", required = false) String name) throws InvalidProtocolBufferException {
        List<Good> goods = goodService.findGoodsFiltered(groupId, brandId, name);
        List<GoodGroup> groups = goodService.findGroupsFiltered(groupId);
        return ResponseEntity.ok(printer.print(buildProtoFromDomain(goods, groups)));
    }

    private Goods.GoodsListResponse buildProtoFromDomain(List<Good> goods, List<GoodGroup> groups) {
        List<Goods.Good> protoGoods = new ArrayList<>();
        List<Goods.Group> protoGroups = new ArrayList<>();
        for (Good good : goods) {
            protoGoods.add(Goods.Good.newBuilder()
                    .setId(good.getId())
                    .setName(good.getName())
                    .setBrandId(good.getBrandId())
                    .setGroupId(good.getGroupId())
                    .setUnit(good.getUnit())
                    .setPackage(good.getInPackage())
                    .setBarCode(good.getBarCode())
                    .setWeight(good.getWeight())
                    .build());
        }
        for (GoodGroup group : groups) {
            protoGroups.add(Goods.Group.newBuilder()
                    .setId(group.getId())
                    .setName(group.getName())
                    .build());
        }
        return Goods.GoodsListResponse.newBuilder()
                .addAllGoods(protoGoods)
                .addAllGroups(protoGroups)
                .build();
    }
}
