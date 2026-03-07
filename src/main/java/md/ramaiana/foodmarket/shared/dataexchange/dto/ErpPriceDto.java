package md.ramaiana.foodmarket.shared.dataexchange.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;
import md.ramaiana.foodmarket.shared.enums.PriceType;

@Getter
@Setter
@XmlRootElement(name = "price")
@XmlAccessorType(XmlAccessType.FIELD)
public final class ErpPriceDto {
    @XmlAttribute
    private String storageCode;
    @XmlAttribute
    private String productCode;
    @XmlAttribute
    private PriceType type;
    @XmlAttribute
    private float price;
}
