package md.ramaiana.foodmarket.shared.dataexchange.dto;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@XmlRootElement(name = "balance-data")
@XmlAccessorType(XmlAccessType.FIELD)
public class BalanceDataDto {
    @XmlElement(name = "price")
    @XmlElementWrapper(name = "prices")
    private List<ErpPriceDto> prices;
    @XmlElement(name = "balance")
    @XmlElementWrapper(name = "balances")
    private List<ErpBalanceDto> balances;

}
