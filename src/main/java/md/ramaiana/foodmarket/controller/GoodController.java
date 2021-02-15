package md.ramaiana.foodmarket.controller;


import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import md.ramaiana.foodmarket.model.Good;
import md.ramaiana.foodmarket.proto.Goods;
import md.ramaiana.foodmarket.service.GoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public ResponseEntity<?> getAllGoods() throws InvalidProtocolBufferException {
        List<Good> goods = goodService.getAllGoods();
        return ResponseEntity.ok(printer.print(buildProtoFromDomain(goods)));
    }

    private Goods.GoodsListResponse buildProtoFromDomain(List<Good> goods) {
        List<Goods.Good> protoGoods = new ArrayList<>();
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
        return Goods.GoodsListResponse.newBuilder()
                .addAllGoods(protoGoods)
                .build();
    }
}
