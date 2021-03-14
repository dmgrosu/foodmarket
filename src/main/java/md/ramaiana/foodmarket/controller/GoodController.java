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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
        List<GoodGroup> groups = goodService.getGroupsHierarchy(groupId);
        return ResponseEntity.ok(printer.print(buildProtoFromDomain(goods, groups)));
    }

    private Goods.GoodsListResponse buildProtoFromDomain(List<Good> goods, List<GoodGroup> groups) {
        return Goods.GoodsListResponse.newBuilder()
                .addAllGoods(mapGoodsToProto(goods))
                .addAllGroups(mapGroupsToProto(groups))
                .build();
    }

    private List<Goods.Good> mapGoodsToProto(List<Good> goods) {
        return goods.stream().map(good -> Goods.Good.newBuilder()
                .setId(good.getId())
                .setName(good.getName())
                .setBrandId(good.getBrandId())
                .setGroupId(good.getGroupId())
                .setUnit(good.getUnit())
                .setPackage(good.getInPackage())
                .setBarCode(good.getBarCode())
                .setWeight(good.getWeight())
                .build()).collect(Collectors.toList());
    }

    private List<Goods.Group> mapGroupsToProto(List<GoodGroup> groups) {
        List<Goods.Group> protoGroups = new ArrayList<>();
        for (GoodGroup group : groups) {
            List<Goods.Group> childrenProto = null;
            if (group.hasChildren()) {
                childrenProto = mapGroupsToProto(group.getChildGroups());
            }
            Goods.Group protoGroup = Goods.Group.newBuilder()
                    .setId(group.getId())
                    .setName(group.getName())
                    .addAllGroups(childrenProto != null ? childrenProto : Collections.emptyList())
                    .build();
            protoGroups.add(protoGroup);
        }
        return protoGroups;
    }
}
