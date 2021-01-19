package com.piotrak.kalah;

import static springfox.documentation.builders.RequestHandlerSelectors.basePackage;
import static springfox.documentation.spi.DocumentationType.SWAGGER_2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
@SpringBootApplication
public class Application {

  private static final String CONTROLLERS_PACKAGE =
    "com.piotrak.kalah.controller";

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Bean
  public Docket swaggerSettings() {
    return new Docket(SWAGGER_2)
      .apiInfo(apiInfo())
      .select()
      .apis(basePackage(CONTROLLERS_PACKAGE))
      .build()
      .useDefaultResponseMessages(false);
  }

  private ApiInfo apiInfo() {
    return new ApiInfoBuilder()
      .title("Kalah API")
      .description("Provides a platform for Kalah gaming")
      .version("1.0.0")
      .termsOfServiceUrl("termsOfServiceUrl")
      .build();
  }

}
