package md.ramaiana.foodmarket.config;

import md.ramaiana.foodmarket.model.Role;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;

/**
 * @author Dmitri Grosu, 2/7/21
 */
@Configuration
public class DataJdbcConfig extends AbstractJdbcConfiguration {
    @Override
    public JdbcCustomConversions jdbcCustomConversions() {
        return new JdbcCustomConversions(Arrays.asList(
                new StringToRoleConverter(),
                new RoleToStringConverter(),
                new TimestampToOffsetDateTimeConverter(),
                new OffsetDateTimeToTimestampConverter()
        ));
    }

    @ReadingConverter
    static class StringToRoleConverter implements Converter<String, Role> {
        @Override
        public Role convert(String s) {
            return Role.fromDbValue(s);
        }
    }

    @WritingConverter
    static class RoleToStringConverter implements Converter<Role, String> {
        @Override
        public String convert(Role role) {
            return role.getDbValue();
        }
    }

    @ReadingConverter
    static class TimestampToOffsetDateTimeConverter implements Converter<Timestamp, OffsetDateTime> {
        @Override
        public OffsetDateTime convert(Timestamp timestamp) {
            return OffsetDateTime.ofInstant(timestamp.toInstant(), ZoneId.of("UTC"));
        }
    }

    @WritingConverter
    static class OffsetDateTimeToTimestampConverter implements Converter<OffsetDateTime, Timestamp> {
        @Override
        public Timestamp convert(OffsetDateTime offsetDateTime) {
            return Timestamp.from(offsetDateTime.toInstant());
        }
    }
}
