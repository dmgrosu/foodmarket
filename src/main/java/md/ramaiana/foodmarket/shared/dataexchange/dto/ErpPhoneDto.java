package md.ramaiana.foodmarket.shared.dataexchange.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlRootElement(name = "client")
@XmlAccessorType(XmlAccessType.FIELD)
public class ErpPhoneDto {
    @XmlAttribute
    private String number;
    @XmlAttribute
    private String name;
}
