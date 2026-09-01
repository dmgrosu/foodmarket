package md.ramaiana.foodmarket.shared.dataexchange.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Root of an {@code orders-data-*.xml} export file: the orders handed to the ERP in one batch.
 * <p>
 * A Lombok class rather than a record, like every other DTO in this package: JAXB needs a no-arg
 * constructor and mutable fields to bind, and cannot marshal a record.
 */
@Getter
@Setter
@NoArgsConstructor
@XmlRootElement(name = "orders-data")
@XmlAccessorType(XmlAccessType.FIELD)
public final class OrdersDataDto {

    @XmlElement(name = "order")
    @XmlElementWrapper(name = "orders")
    private List<ErpOrderDto> orders;

    public OrdersDataDto(List<ErpOrderDto> orders) {
        this.orders = orders;
    }
}
