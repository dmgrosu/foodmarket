package md.ramaiana.foodmarket.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Dmitri Grosu (dmitri.grosu@codefactorygroup.com), 3/7/21
 */
@Getter
@Value
@Builder
public class GoodsReadResult {
    @Builder.Default
    List<GoodGroup> groups = new ArrayList<>();
    @Builder.Default
    List<Good> goods = new ArrayList<>();
    @Builder.Default
    Set<Brand> brands = new HashSet<>();
}
