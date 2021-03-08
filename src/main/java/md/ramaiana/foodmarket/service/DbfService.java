package md.ramaiana.foodmarket.service;

import com.linuxense.javadbf.DBFReader;
import com.linuxense.javadbf.DBFRow;
import lombok.extern.slf4j.Slf4j;
import md.ramaiana.foodmarket.model.Brand;
import md.ramaiana.foodmarket.model.Good;
import md.ramaiana.foodmarket.model.GoodGroup;
import md.ramaiana.foodmarket.model.GoodsReadResult;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dmitri Grosu (dmitri.grosu@gmail.com), 3/7/21
 */
@Service
@Slf4j
public class DbfService {

    /**
     * This method is used to read Goods data from DBF file
     * Required DBF file structure:
     * <p>
     * ERP_ID (Character(15)) - good ERP code
     * NAME (Character(150)) - good name
     * BR_ID (Character(10)) - brand ERP code
     * BR_NAME (Character(40)) - brand name
     * UNIT (Character(10)) - unit name
     * GR_ID (Character(15)) - good group ERP code
     * PACK (Numeric(10,2)) - good's in package value
     * WEIGHT (Numeric(8,3)) - weight of good
     * BARCODE (Character(15)) - good's barcode
     * PRICE (Numeric(12,2)) - good's price
     * TYPE (Character(1)) - type: 0 - good, 1 - group
     * </p>
     * @return instance of GoodsReadResult, containing lists of Good, Groups and Brands that were read from file
     */
    public GoodsReadResult readGoodsFromFile(String filePath) {
        Map<String, GoodGroup> groups = new HashMap<>();
        Map<String, Good> goods = new HashMap<>();
        Map<String, Brand> brands = new HashMap<>();
        Map<String, String[]> erpCodes = new HashMap<>();

        try (DBFReader dbfReader = new DBFReader(new FileInputStream(filePath), Charset.forName("cp1251"))) {
            DBFRow dbfRow;
            while ((dbfRow = dbfReader.nextRow()) != null) {
                try {
                    int goodType = Integer.parseInt(dbfRow.getString("TYPE"));
                    String goodErpCode = dbfRow.getString("ERP_ID");
                    if (goodType == 0) {
                        goods.put(goodErpCode, mapDbfRowToGood(dbfRow));
                    } else {
                        groups.put(goodErpCode, mapDbfRowToGroup(dbfRow));
                    }
                    String brandErpCode = dbfRow.getString("BR_ID");
                    brands.put(brandErpCode, Brand.builder()
                            .erpCode(brandErpCode)
                            .name(dbfRow.getString("BR_NAME"))
                            .build());
                    String parentErpCode = dbfRow.getString("GR_ID");
                    String[] codes = new String[2];
                    codes[0] = parentErpCode.isEmpty() ? null : parentErpCode;
                    codes[1] = brandErpCode.isEmpty() ? null : brandErpCode;
                    erpCodes.put(goodErpCode, codes);
                } catch (Exception ex) {
                    log.error("Error reading DBF row: " + ex.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error loading goods from file: " + e.getMessage());
        }
        return GoodsReadResult.builder()
                .goods(goods)
                .groups(groups)
                .brands(brands)
                .erpCodes(erpCodes)
                .build();
    }

    private GoodGroup mapDbfRowToGroup(DBFRow dbfRow) {
        return GoodGroup.builder()
                .erpCode(dbfRow.getString("ERP_ID"))
                .name(dbfRow.getString("NAME"))
                .build();
    }

    private Good mapDbfRowToGood(DBFRow dbfRow) {
        return Good.builder()
                .erpCode(dbfRow.getString("ERP_ID"))
                .name(dbfRow.getString("NAME"))
                .unit(dbfRow.getString("UNIT"))
                .inPackage(dbfRow.getFloat("PACK"))
                .weight(dbfRow.getFloat("WEIGHT"))
                .barCode(dbfRow.getString("BARCODE"))
                .price(dbfRow.getFloat("PRICE"))
                .build();
    }
}
