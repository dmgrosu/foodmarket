package md.ramaiana.foodmarket.shared.dataexchange.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlRootElement(name = "balance")
@XmlAccessorType(XmlAccessType.FIELD)
public class ErpBalanceDto {
    @XmlAttribute
    private String storageErpCode;
    @XmlAttribute
    private String productErpCode;
    @XmlAttribute
    private float quantity;
}
