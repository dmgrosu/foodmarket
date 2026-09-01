package md.ramaiana.foodmarket.shared.dataexchange.core.usecase;

import jakarta.annotation.Nullable;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import md.ramaiana.foodmarket.domain.brand.data.BrandEntity;
import md.ramaiana.foodmarket.domain.price.data.PriceEntity;
import md.ramaiana.foodmarket.domain.product.core.usecase.ProductLoadUseCase;
import md.ramaiana.foodmarket.domain.product.data.ProductEntity;
import md.ramaiana.foodmarket.domain.product.data.ProductGroupEntity;
import md.ramaiana.foodmarket.domain.storage.core.usecase.StorageSearchUseCase;
import md.ramaiana.foodmarket.domain.storage.data.StorageEntity;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.dataexchange.core.data.ProductReadResult;
import md.ramaiana.foodmarket.shared.dataexchange.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.oxm.Unmarshaller;

import javax.xml.transform.stream.StreamSource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
    private final StorageSearchUseCase storageSearch;
    private final Unmarshaller unmarshaller;


    public void execute() {
        try {
            Path filePath = Path.of(exchangeFolderPath).resolve(PRODUCTS_DATA_FILE);
            CatalogDto catalogDto = (CatalogDto) unmarshaller.unmarshal(getSource(filePath));
            productLoadUseCase.execute(mapToReadResult(catalogDto));
            deleteFile(filePath);
        } catch (FileNotFoundException ex) {
            log.warn("Skip products import - no {} file found", PRODUCTS_DATA_FILE);
        } catch (Exception ex) {
            log.error("Error while importing products: {}", ex.getMessage(), ex);
        }
    }

    @NonNull
    private StreamSource getSource(Path file) throws FileNotFoundException {
        return new StreamSource(new FileInputStream(file.toFile()));
    }

    private void deleteFile(Path filePath) {
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("Error while deleting file {}: {}", filePath, e.getMessage());
        }
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
        return nullSafe(dtoGroups).stream()
                .collect(Collectors.toMap(ErpGroupDto::getCode,
                        dto -> new ProductGroupEntity(dto.getName(), dto.getCode()),
                        (_, last) -> last));
    }

    private Map<String, ProductEntity> toProducts(List<ErpProductDto> dtoProducts) {
        return nullSafe(dtoProducts).stream()
                .collect(Collectors.toMap(ErpProductDto::getCode,
                        this::toProduct,
                        (_, last) -> last));
    }

    private ProductEntity toProduct(ErpProductDto dto) {
        return new ProductEntity(
                dto.getName(),
                dto.getUnit(),
                dto.getPackSize(),
                dto.getCode(),
                toBarCode(dto.getCodes()),
                dto.getWeight(),
                toPrices(dto.getPrices())
        );
    }

    private Set<PriceEntity> toPrices(List<ErpPriceDto> dtoPrices) {
        return nullSafe(dtoPrices).stream()
                .map(this::toPrice)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Nullable
    private PriceEntity toPrice(ErpPriceDto dto) {
        try {
            AggregateReference<StorageEntity, Integer> storage = AggregateReference.to(
                    storageSearch.findByErpCode(dto.getStorageCode()).getId()
            );
            return new PriceEntity(dto.getType(), storage, dto.getPrice());
        } catch (Exception ex) {
            log.error("Error while mapping price {}-{}: {}", dto.getType(), dto.getPrice(), ex.getMessage());
            return null;
        }
    }

    private String toBarCode(List<ErpProductCodeDto> dtoCodes) {
        return nullSafe(dtoCodes).stream()
                .filter(Objects::nonNull)
                .filter(it -> "barCode".equalsIgnoreCase(it.getName()))
                .findFirst()
                .map(ErpProductCodeDto::getValue)
                .orElse(null);
    }

    private Map<String, BrandEntity> toBrands(List<ErpBrandDto> dtoBrands) {
        return nullSafe(dtoBrands).stream()
                .collect(Collectors.toMap(ErpBrandDto::getCode,
                        dto -> new BrandEntity(dto.getName(), dto.getCode()),
                        (_, last) -> last));
    }

    private Map<String, String[]> toErpCodes(CatalogDto catalogDto) {
        Map<String, String[]> result = new HashMap<>();
        for (ErpGroupDto group : nullSafe(catalogDto.getGroups())) {
            String[] codes = new String[2];
            codes[0] = group.getParentCode();
            codes[1] = null;
            result.put(group.getCode(), codes);
        }
        for (ErpProductDto product : nullSafe(catalogDto.getProducts())) {
            String[] codes = result.getOrDefault(product.getCode(), new String[2]);
            codes[0] = product.getGroupCode();
            codes[1] = product.getBrandCode();
            result.put(product.getCode(), codes);
        }
        return result;
    }

    /** A section absent from the file arrives as {@code null} rather than as an empty list. */
    private <T> List<T> nullSafe(List<T> list) {
        return list == null ? List.of() : list;
    }

}
