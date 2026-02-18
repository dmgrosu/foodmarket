package md.ramaiana.foodmarket.service.dataexchange;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import md.ramaiana.foodmarket.model.Brand;
import md.ramaiana.foodmarket.model.Product;
import md.ramaiana.foodmarket.model.ProductGroup;
import md.ramaiana.foodmarket.model.ProductReadResult;
import md.ramaiana.foodmarket.service.ProductService;
import md.ramaiana.foodmarket.service.dataexchange.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.stereotype.Service;

import javax.xml.transform.stream.StreamSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class XmlDataExchangeService implements DataExchangeService {

    @Setter
    @Value("${dataFilePath}")
    private String filePath;
    private final ProductService productService;
    private final Marshaller marshaller;
    private final Unmarshaller unmarshaller;

    @Override
    public void importProducts() throws IOException {
        CatalogDto catalogDto = (CatalogDto) unmarshaller.unmarshal(new StreamSource(new FileInputStream(filePath)));
        productService.loadProducts(mapToReadResult(catalogDto));
    }

    @Override
    public void exportOrders() throws IOException {
        // TODO
    }

    private ProductReadResult mapToReadResult(CatalogDto catalogDto) {
        return ProductReadResult.builder()
                .groups(toGroups(catalogDto.getGroups()))
                .products(toProducts(catalogDto.getProducts()))
                .brands(toBrands(catalogDto.getBrands()))
                .erpCodes(toErpCodes(catalogDto))
                .build();
    }

    private Map<String, ProductGroup> toGroups(List<ErpGroupDto> dtoGroups) {
        return dtoGroups.stream()
                .collect(Collectors.toMap(ErpGroupDto::getCode,
                        dto -> new ProductGroup(dto.getName(), dto.getCode()),
                        (a, b) -> b));
    }

    private Map<String, Product> toProducts(List<ErpProductDto> dtoProducts) {
        return dtoProducts.stream()
                .collect(Collectors.toMap(ErpProductDto::getCode,
                        this::toProduct,
                        (a, b) -> b));
    }

    private Product toProduct(ErpProductDto dto) {
        return new Product(
                dto.getName(),
                dto.getUnit(),
                dto.getPackSize(),
                dto.getCode(),
                toBarCode(dto.getCodes()),
                dto.getWeight()
        );
    }

    private String toBarCode(List<ErpProductCodeDto> dtoCodes) {
        return dtoCodes.stream()
                .filter(Objects::nonNull)
                .filter(it -> it.getName().equalsIgnoreCase("barCode"))
                .findFirst()
                .map(ErpProductCodeDto::getValue)
                .orElse(null);
    }

    private Map<String, Brand> toBrands(List<ErpBrandDto> dtoBrands) {
        return dtoBrands.stream()
                .collect(Collectors.toMap(ErpBrandDto::getCode,
                        dto -> Brand.builder()
                                .erpCode(dto.getCode())
                                .name(dto.getName())
                                .build(),
                        (a, b) -> b));
    }

    private Map<String, String[]> toErpCodes(CatalogDto catalogDto) {
        Map<String, String[]> result = new HashMap<>();
        for (ErpGroupDto group : catalogDto.getGroups()) {
            String[] codes = new String[2];
            codes[0] = group.getParentCode();
            codes[1] = null;
            result.put(group.getCode(), codes);
        }
        for (ErpProductDto product : catalogDto.getProducts()) {
            String[] codes = result.getOrDefault(product.getCode(), new String[2]);
            codes[0] = product.getGroupCode();
            codes[1] = product.getBrandCode();
            result.put(product.getCode(), codes);
        }
        return result;
    }

}
