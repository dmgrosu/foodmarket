package md.ramaiana.foodmarket.shared.dataexchange.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * One line of an exported order. The product is named by its ERP code, the same code
 * {@code ImportProductsUseCase} files products under.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public final class ErpOrderItemDto {

    @XmlAttribute
    private String productCode;
    @XmlAttribute
    private float quantity;
    @XmlAttribute
    private float price;
    @XmlAttribute
    private float sum;
    @XmlAttribute
    private float weight;

    /**
     * Constructor.
     */
    public ErpOrderItemDto(String productCode, float quantity, float price, float sum, float weight) {
        this.productCode = productCode;
        this.quantity = quantity;
        this.price = price;
        this.sum = sum;
        this.weight = weight;
    }
}
