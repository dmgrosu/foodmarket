package md.ramaiana.foodmarket.shared.util;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
class SecureTokenGeneratorTest {

    private final SecureTokenGenerator generator = new SecureTokenGenerator();

    @Test
    void generate_should_produce_url_safe_tokens_of_expected_length() {
        String token = generator.generate();

        assertThat(token).hasSizeGreaterThanOrEqualTo(43);
        assertThat(token).matches("^[A-Za-z0-9_-]+$");
    }

    @Test
    void generate_should_produce_unique_tokens() {
        Set<String> tokens = IntStream.range(0, 1000)
            .mapToObj(i -> generator.generate())
            .collect(java.util.stream.Collectors.toCollection(HashSet::new));

        assertThat(tokens).hasSize(1000);
    }

    @Test
    void hash_should_be_stable_for_the_same_input() {
        String token = generator.generate();

        assertThat(generator.hash(token)).isEqualTo(generator.hash(token));
    }

    @Test
    void hash_should_differ_from_the_raw_input() {
        String token = generator.generate();

        assertThat(generator.hash(token)).isNotEqualTo(token);
    }

    @Test
    void hash_should_differ_per_input() {
        String tokenA = generator.generate();
        String tokenB = generator.generate();

        assertThat(generator.hash(tokenA)).isNotEqualTo(generator.hash(tokenB));
    }
}
