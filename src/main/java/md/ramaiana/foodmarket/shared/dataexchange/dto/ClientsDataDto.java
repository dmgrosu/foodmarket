package md.ramaiana.foodmarket.shared.dataexchange.dto;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@XmlRootElement(name = "clients-data")
@XmlAccessorType(XmlAccessType.FIELD)
public class ClientsDataDto {
    @XmlElement(name = "client")
    @XmlElementWrapper(name = "clients")
    List<ErpClientDto> clients;
}
