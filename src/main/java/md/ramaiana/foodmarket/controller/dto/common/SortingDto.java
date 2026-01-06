package md.ramaiana.foodmarket.controller.dto.common;

public record SortingDto(
        String columnName,
        Direction direction
) {
    public enum Direction {
        ASC, DESC
    }
}
