package md.ramaiana.foodmarket.domain.product.core.usecase;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
class ProductGroupNamingTest {

    @Test
    void takes_the_words_every_product_in_the_group_begins_with() {
        String name = ProductGroupNaming.deriveFrom(List.of(
                "POSUDA CHASKA 18442 ceramic 240ml 1 12",
                "POSUDA CHASKA 20115 ceramic 300ml 1 12",
                "POSUDA CHASKA 33107 glass 200ml 1 24"));

        assertThat(name).isEqualTo("POSUDA CHASKA");
    }

    @Test
    void stops_at_the_first_word_the_products_disagree_on() {
        String name = ProductGroupNaming.deriveFrom(List.of(
                "SPICES pepper black 25gr",
                "SPICES pepper white 25gr"));

        assertThat(name).isEqualTo("SPICES pepper");
    }

    @Test
    void drops_the_separator_the_shared_prefix_stops_on() {
        String name = ProductGroupNaming.deriveFrom(List.of(
                "TOWELS Pattern Terry, 002-166-1000",
                "TOWELS Pattern Terry, 002-166-2000"));

        assertThat(name).isEqualTo("TOWELS Pattern Terry");
    }

    @Test
    void caps_a_single_product_group_at_the_category_words() {
        // Left uncapped this group would be labelled with the whole product name.
        String name = ProductGroupNaming.deriveFrom(List.of(
                "ASCANIA TEA-CONCENTRATE 375gr Lime plus bergamot TUBE 1 10"));

        assertThat(name).isEqualTo("ASCANIA TEA-CONCENTRATE 375gr Lime plus");
    }

    @Test
    void handles_a_group_whose_products_share_only_their_first_word() {
        String name = ProductGroupNaming.deriveFrom(List.of("CHEESE gouda 1kg", "CHEESE brie 200gr"));

        assertThat(name).isEqualTo("CHEESE");
    }

    @Test
    void gives_nothing_back_when_there_is_nothing_to_derive_from() {
        assertThat(ProductGroupNaming.deriveFrom(List.of())).isNull();
        assertThat(ProductGroupNaming.deriveFrom(List.of("  "))).isNull();
        assertThat(ProductGroupNaming.deriveFrom(List.of("-", "- x"))).isNull();
    }
}
