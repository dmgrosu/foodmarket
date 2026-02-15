package md.ramaiana.foodmarket.service.dataexchange.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlRootElement(name = "brand")
@XmlAccessorType(XmlAccessType.FIELD)
public final class ErpBrandDto {
    @XmlAttribute
    private String code;
    @XmlAttribute
    private String name;
}
