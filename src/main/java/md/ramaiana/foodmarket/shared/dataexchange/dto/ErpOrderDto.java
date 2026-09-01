package md.ramaiana.foodmarket.shared.dataexchange.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * One order in an export file.
 * <p>
 * {@code id} is our order id and is stable across re-exports. It is the ERP's deduplication key: a
 * batch whose file was written but whose orders could not be marked exported is written again on the
 * next cycle, so the ERP must be able to recognise an order it has already taken.
 * <p>
 * The client is named twice on purpose. {@code clientCode} is the ERP's own key, which is what it
 * should match on, but it is null for a client imported before the code was persisted;
 * {@code clientIdno} is the fiscal code, always present, and is the fallback.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public final class ErpOrderDto {

    @XmlAttribute
    private Integer id;
    @XmlAttribute
    private String createdAt;
    @XmlAttribute
    private String placedAt;
    @XmlAttribute
    private String clientCode;
    @XmlAttribute
    private String clientIdno;
    @XmlAttribute
    private String storageCode;
    @XmlAttribute
    private String priceType;
    @XmlAttribute
    private float totalSum;
    @XmlAttribute
    private float totalWeight;
    @XmlElement(name = "item")
    private List<ErpOrderItemDto> items;

    /**
     * Constructor.
     */
    public ErpOrderDto(Integer id, String createdAt, String placedAt, String clientCode,
                       String clientIdno, String storageCode, String priceType, float totalSum,
                       float totalWeight, List<ErpOrderItemDto> items) {
        this.id = id;
        this.createdAt = createdAt;
        this.placedAt = placedAt;
        this.clientCode = clientCode;
        this.clientIdno = clientIdno;
        this.storageCode = storageCode;
        this.priceType = priceType;
        this.totalSum = totalSum;
        this.totalWeight = totalWeight;
        this.items = items;
    }
}
