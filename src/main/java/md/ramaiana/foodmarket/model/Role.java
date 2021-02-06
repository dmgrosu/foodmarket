package md.ramaiana.foodmarket.model;

public enum Role {
    ADMIN("admin"),
    USER("user");

    private String dbValue;

    Role(String dbValue) {
        this.dbValue = dbValue;
    }

    public static Role fromDbValue(String dbValue) {
        for (Role role : values()) {
            if (role.dbValue.equals(dbValue)) {
                return role;
            }
        }
        throw new IllegalArgumentException(String.format("Illegal role value [%s]", dbValue));
    }
}
