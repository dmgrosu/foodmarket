package md.ramaiana.foodmarket.shared.dataexchange.dto;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@XmlRootElement(name = "client")
@XmlAccessorType(XmlAccessType.FIELD)
public class ErpClientDto {
    @XmlAttribute
    private String code;
    @XmlAttribute
    private String name;
    @XmlAttribute
    private String idno;
    @XmlElement(name = "address")
    List<ErpAddressDto> addresses;
    @XmlElement(name = "phone")
    List<ErpPhoneDto> phones;
    @XmlElement
    private String email;
}
