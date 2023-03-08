package gms.shared.stationdefinition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {"gms.shared.spring", "gms.shared.stationdefinition", "gms.shared.emf.stationdefinition"},
  excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "gms\\.shared\\.spring\\.persistence\\..*"))
public class StationDefinitionApplication {
  private static final Logger logger = LoggerFactory.getLogger(StationDefinitionApplication.class);

  public static void main(String[] args) {
    logger.info("Starting station-definition-service");

    new SpringApplicationBuilder(StationDefinitionApplication.class)
      .run(args);
  }
}
