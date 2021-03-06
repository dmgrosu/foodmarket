package md.ramaiana.foodmarket.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Dmitri Grosu (dmitri.grosu@gmail.com), 3/7/21
 */
@Getter
@Value
@Builder
public class GoodsReadResult {
    /**
     * Map, containing groups, read from file
     * key - group ERP code, value - group itself
     */
    @Builder.Default
    Map<String, GoodGroup> groups = new HashMap<>();
    /**
     * Map containing good, read from file
     * key - good ERP code, value - good itself
     */
    @Builder.Default
    Map<String, Good> goods = new HashMap<>();
    /**
     * Map containing brands, read from file
     * key - brand ERP code, value - brand itself
     */
    @Builder.Default
    Map<String, Brand> brands = new HashMap<>();
    /**
     * Map containing ERP child-parent-brand relations:
     * key - good/group ERP code
     * value - array, containing:
     * [0] parent ERP code
     * [1] brand ERP code
     */
    @Builder.Default
    Map<String, String[]> erpCodes = new HashMap<>();
}
