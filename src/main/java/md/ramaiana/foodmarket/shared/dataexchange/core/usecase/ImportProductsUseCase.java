package md.ramaiana.foodmarket.shared.dataexchange.core.usecase;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import md.ramaiana.foodmarket.domain.brand.data.BrandEntity;
import md.ramaiana.foodmarket.domain.product.core.usecase.ProductLoadUseCase;
import md.ramaiana.foodmarket.domain.product.data.ProductEntity;
import md.ramaiana.foodmarket.domain.product.data.ProductGroupEntity;
import md.ramaiana.foodmarket.shared.dataexchange.core.data.ProductReadResult;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.dataexchange.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.oxm.Unmarshaller;

import javax.xml.transform.stream.StreamSource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class ImportProductsUseCase {

    private static final String PRODUCTS_DATA_FILE = "products-data.xml";

    @Setter
    @Value("${dataFolderPath}")
    private String exchangeFolderPath;
    private final ProductLoadUseCase productLoadUseCase;
    private final Unmarshaller unmarshaller;


    public void execute() {
        try {
            String filePath = exchangeFolderPath + PRODUCTS_DATA_FILE;
            CatalogDto catalogDto = (CatalogDto) unmarshaller.unmarshal(getSource(filePath));
            productLoadUseCase.execute(mapToReadResult(catalogDto));
        } catch (FileNotFoundException ex) {
            log.warn("Skip products import - no {} file found", PRODUCTS_DATA_FILE);
        } catch (Exception ex) {
            log.error("Error while importing products: {}", ex.getMessage(), ex);
        }
    }

    @NonNull
    private StreamSource getSource(String file) throws FileNotFoundException {
        return new StreamSource(new FileInputStream(file));
    }

    private ProductReadResult mapToReadResult(CatalogDto catalogDto) {
        return ProductReadResult.builder()
                .groups(toGroups(catalogDto.getGroups()))
                .products(toProducts(catalogDto.getProducts()))
                .brands(toBrands(catalogDto.getBrands()))
                .erpCodes(toErpCodes(catalogDto))
                .build();
    }

    private Map<String, ProductGroupEntity> toGroups(List<ErpGroupDto> dtoGroups) {
        return dtoGroups.stream()
                .collect(Collectors.toMap(ErpGroupDto::getCode,
                        dto -> new ProductGroupEntity(dto.getName(), dto.getCode()),
                        (a, b) -> b));
    }

    private Map<String, ProductEntity> toProducts(List<ErpProductDto> dtoProducts) {
        return dtoProducts.stream()
                .collect(Collectors.toMap(ErpProductDto::getCode,
                        this::toProduct,
                        (a, b) -> b));
    }

    private ProductEntity toProduct(ErpProductDto dto) {
        return new ProductEntity(
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

    private Map<String, BrandEntity> toBrands(List<ErpBrandDto> dtoBrands) {
        return dtoBrands.stream()
                .collect(Collectors.toMap(ErpBrandDto::getCode,
                        dto -> new BrandEntity(dto.getCode(), dto.getName()),
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
