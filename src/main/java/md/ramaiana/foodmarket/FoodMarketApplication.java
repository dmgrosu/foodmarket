package md.ramaiana.foodmarket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FoodMarketApplication {

    static void main(String[] args) {
        SpringApplication.run(FoodMarketApplication.class, args);
    }

}
