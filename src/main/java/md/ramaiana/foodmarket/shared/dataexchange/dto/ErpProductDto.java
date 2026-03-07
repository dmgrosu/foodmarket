package md.ramaiana.foodmarket.shared.dataexchange.dto;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@XmlRootElement(name = "product")
@XmlAccessorType(XmlAccessType.FIELD)
public final class ErpProductDto {
    @XmlAttribute
    private String code;
    @XmlAttribute
    private String name;
    @XmlAttribute
    private String groupCode;
    @XmlAttribute
    private String brandCode;
    @XmlAttribute
    private float packSize;
    @XmlAttribute
    private String unit;
    @XmlAttribute
    private float weight;
    @XmlElement(name = "code")
    @XmlElementWrapper(name = "codes")
    private List<ErpProductCodeDto> codes;
}
