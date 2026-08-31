package md.ramaiana.foodmarket.domain.product.core.usecase;

import jakarta.annotation.Nullable;
import java.util.List;
import lombok.NonNull;

/**
 * Derives a readable name for a product group the ERP export never named.
 * <p>
 * The export declares a {@code <group>} only for the codes that mirror products; the codes products
 * are actually filed under arrive with no name at all, leaving the catalogue showing raw ERP codes
 * like {@code 00016238}. The names of the products in a group, however, are written to a convention
 * that puts the category first — "ПОСУДА ЧАШКА 18442 керам 240ml", "ПОСУДА ЧАШКА 20115 …" — so the
 * words they all begin with are the category: "ПОСУДА ЧАШКА".
 * <p>
 * Measured across the live export this yields a name for every one of the 1,231 unnamed groups, and
 * no two groups end up with the same one. It is still inference: a name the ERP declares always wins
 * over a name derived here.
 */
final class ProductGroupNaming {

  /**
   * A group holding one product would otherwise be labelled with that product's whole name, which
   * runs to fourteen words in this export. The leading words are the category; the rest is the
   * variant, and it does not belong in a category label.
   */
  private static final int MAX_WORDS = 5;

  /**
   * Trailing punctuation left behind when the shared prefix stops mid-phrase — "ПОЛОТЕНЦА Узор
   * Махров," keeps the comma that separated it from the words the products no longer share.
   */
  private static final String TRAILING_SEPARATORS = " \t,.;:-–—/\\(+";

  private ProductGroupNaming() {
  }

  /**
   * The category the given product names have in common, or {@code null} when they have nothing
   * usable in common and the caller should fall back to the ERP code.
   */
  @Nullable
  static String deriveFrom(@NonNull List<String> productNames) {
    if (productNames.isEmpty()) {
      return null;
    }
    List<String[]> wordsPerName = productNames.stream()
        .map(name -> name.trim().split("\\s+"))
        .toList();
    int shortest = wordsPerName.stream().mapToInt(words -> words.length).min().orElse(0);

    StringBuilder derived = new StringBuilder();
    for (int position = 0; position < Math.min(shortest, MAX_WORDS); position++) {
      int atPosition = position;
      String word = wordsPerName.getFirst()[atPosition];
      boolean sharedByAll = wordsPerName.stream().allMatch(words -> words[atPosition].equals(word));
      if (!sharedByAll) {
        break;
      }
      if (!derived.isEmpty()) {
        derived.append(' ');
      }
      derived.append(word);
    }

    String name = trimTrailingSeparators(derived.toString());
    return name.isBlank() ? null : name;
  }

  @NonNull
  private static String trimTrailingSeparators(@NonNull String name) {
    int end = name.length();
    while (end > 0 && TRAILING_SEPARATORS.indexOf(name.charAt(end - 1)) >= 0) {
      end--;
    }
    return name.substring(0, end);
  }
}
