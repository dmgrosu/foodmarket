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

    @GetMapping("/listGroups")
    public ResponseEntity<?> getGroupsByParent(@RequestParam(value = "parentGroupId", required = false) Integer parentGroupId) throws InvalidProtocolBufferException {
        List<GoodGroup> groups = goodService.getGroupsHierarchy(parentGroupId);
        return ResponseEntity.ok(printer.print(buildGoodsListResponse(Collections.emptyList(), groups)));
    }

    @GetMapping("/listGoods")
    public ResponseEntity<?> getGoodsByGroup(@RequestParam("groupId") Integer groupId) throws InvalidProtocolBufferException {
        List<Good> goods = goodService.findGoodsFiltered(groupId, null, null);
        return ResponseEntity.ok(printer.print(buildGoodsListResponse(goods, Collections.emptyList())));
    }

    private Goods.GoodsListResponse buildGoodsListResponse(List<Good> goods, List<GoodGroup> groups) {
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
                .setPrice(good.getPrice())
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
