package md.ramaiana.foodmarket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataExchangeConfig {

    @Bean
    public Marshaller marshaller() {
        return getMarshaller();
    }

    @Bean
    public Unmarshaller unmarshaller() {
        return getMarshaller();
    }

    private Jaxb2Marshaller getMarshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        Map<String, Object> props = new HashMap<>();
        props.put("jaxb.formatted.output", true);
        // Pinned rather than left to the platform default. Inbound ERP files are Windows-1251, but we
        // own the outbound side; if the ERP reader turns out to assume 1251, this is the one line to
        // change. Marshalling only - unmarshalling reads the encoding declared by the file.
        props.put("jaxb.encoding", "UTF-8");
        marshaller.setPackagesToScan("md.ramaiana.foodmarket.shared.dataexchange.dto");
        marshaller.setMarshallerProperties(props);
        return marshaller;
    }

}
