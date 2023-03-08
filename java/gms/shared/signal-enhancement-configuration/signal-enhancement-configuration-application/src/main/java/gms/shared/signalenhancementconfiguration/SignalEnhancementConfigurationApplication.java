package gms.shared.signalenhancementconfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration;

@SpringBootApplication(exclude = {
  SqlInitializationAutoConfiguration.class,
  DataSourceAutoConfiguration.class,
  DataSourceTransactionManagerAutoConfiguration.class,
  HibernateJpaAutoConfiguration.class,
  JdbcTemplateAutoConfiguration.class
})
public class SignalEnhancementConfigurationApplication {
  private static final Logger logger = LoggerFactory.getLogger(SignalEnhancementConfigurationApplication.class);

  public static void main(String[] args) {
    logger.info("Starting signal enhancement configuration manager");

    SpringApplication.run(SignalEnhancementConfigurationApplication.class, args);
  }
}
