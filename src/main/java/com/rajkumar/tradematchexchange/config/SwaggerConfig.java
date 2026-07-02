package com.rajkumar.tradematchexchange.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI tradeMatchExchangeAPI() {

        return new OpenAPI()

                .info(

                        new Info()

                                .title("TradeMatchExchange API")

                                .description(
                                        """
                                        Production-style Electronic Trade Matching Platform

                                        Features:
                                        • Order Management
                                        • Price-Time Priority Matching
                                        • Market Depth
                                        • Trade Execution
                                        • Risk Validation

                                        Built using Spring Boot and Java.
                                        """)

                                .version("1.0.0")

                                .contact(

                                        new Contact()

                                                .name("Rajkumar Vijayan")

                                                .email("rajkumar.vijayan98@gmail.com")

                                                .url("https://www.linkedin.com/in/rajkumar-vijayan-0135a8338/")
                                )

                                .license(

                                        new License()

                                                .name("MIT License")

                                                .url("https://opensource.org/licenses/MIT")
                                )
                )

                .externalDocs(

                        new ExternalDocumentation()

                                .description("GitHub Repository")

                                .url("https://github.com/Rajkumar0863/TradeMatchExchange")
                );
    }
}