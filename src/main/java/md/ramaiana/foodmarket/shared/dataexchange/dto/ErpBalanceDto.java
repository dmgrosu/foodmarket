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
@XmlRootElement(name = "balance")
@XmlAccessorType(XmlAccessType.FIELD)
public final class ErpBalanceDto {
    @XmlAttribute
    private String storageCode;
    @XmlAttribute
    private String productCode;
    @XmlAttribute
    private float quantity;
}
