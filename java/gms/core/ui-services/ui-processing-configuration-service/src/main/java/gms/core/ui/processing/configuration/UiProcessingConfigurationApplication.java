package gms.core.ui.processing.configuration;


import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

/**
 * UI Processing Configuration Spring Application for running the interactive analysis config service.
 */
@SpringBootApplication
@ComponentScan(basePackages = {"gms.core.ui.processing.configuration", "gms.shared.spring"})
public class UiProcessingConfigurationApplication {

  public static void main(String[] args) {

    new SpringApplicationBuilder(UiProcessingConfigurationApplication.class)
      .run(args);
  }
}
