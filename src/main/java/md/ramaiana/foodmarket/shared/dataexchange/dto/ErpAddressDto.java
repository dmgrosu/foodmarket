package md.ramaiana.foodmarket.shared.dataexchange.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;
import md.ramaiana.foodmarket.shared.enums.AddressType;

@Getter
@Setter
@XmlRootElement(name = "address")
@XmlAccessorType(XmlAccessType.FIELD)
public class ErpAddressDto {
    @XmlAttribute
    private AddressType type;
    @XmlAttribute(name = "address")
    private String fullAddress;
    @XmlAttribute(name = "desrc")
    private String description;
}
