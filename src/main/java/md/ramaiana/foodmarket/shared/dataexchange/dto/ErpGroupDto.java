package md.ramaiana.foodmarket.shared.dataexchange.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@XmlRootElement(name = "group")
@XmlAccessorType(XmlAccessType.FIELD)
public final class ErpGroupDto {
    @XmlAttribute
    private String code;
    @XmlAttribute
    private String name;
    @XmlAttribute
    private String parentCode;
}
