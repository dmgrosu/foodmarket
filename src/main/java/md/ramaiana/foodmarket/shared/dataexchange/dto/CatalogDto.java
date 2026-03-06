package md.ramaiana.foodmarket.shared.dataexchange.dto;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@XmlRootElement(name = "catalog")
@XmlAccessorType(XmlAccessType.FIELD)
public final class CatalogDto {
    @XmlElement(name = "group")
    @XmlElementWrapper(name = "groups")
    private List<ErpGroupDto> groups;
    @XmlElement(name = "brand")
    @XmlElementWrapper(name = "brands")
    private List<ErpBrandDto> brands;
    @XmlElement(name = "product")
    @XmlElementWrapper(name = "products")
    private List<ErpProductDto> products;
}
