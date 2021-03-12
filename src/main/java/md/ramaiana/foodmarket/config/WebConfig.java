package md.ramaiana.foodmarket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.protobuf.ProtobufJsonFormatHttpMessageConverter;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Dmitri Grosu (dmitri.grosu@gmail.com), 2/8/21
 */
@Configuration
@EnableScheduling
public class WebConfig {
    @Bean
    ProtobufJsonFormatHttpMessageConverter protobufHttpMessageConverter() {
        return new ProtobufJsonFormatHttpMessageConverter();
    }
}
