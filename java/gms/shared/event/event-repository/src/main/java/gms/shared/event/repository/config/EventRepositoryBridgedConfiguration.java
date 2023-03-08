package gms.shared.event.repository.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * This configuration exists solely to be able to scan external package components,
 * such as {@link gms.shared.signaldetection.database.connector.AssocDatabaseConnector}
 */
@Configuration
@ComponentScan("gms.shared.signaldetection.database.connector")
public class EventRepositoryBridgedConfiguration {

}
