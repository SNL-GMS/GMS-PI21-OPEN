package gms.shared.workflow.accessor.configuration;

import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.workflow.cache.IntervalCache;
import gms.shared.workflow.cache.util.WorkflowCacheFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * Configuration used for initializing {@link gms.shared.workflow.accessor.WorkflowAccessor}s for use
 */
@Configuration
@DependsOn("workflow-managerConfiguration")
public class WorkflowAcessorConfiguration {

  private static final Logger logger = LoggerFactory.getLogger(WorkflowAcessorConfiguration.class);

  /**
   * Sets up and returns a new {@link IntervalCache} ready for use
   *
   * @param systemConfig System configuration used to set up the cache
   * @return A new, initialized {@link IntervalCache}
   */
  @Bean
  @Autowired
  public IntervalCache intervalCache(SystemConfig systemConfig) {
    try {
      WorkflowCacheFactory.setUpCache(systemConfig);
    } catch (IllegalStateException e) {
      logger.warn("Cache already initialized: ", e);
    }

    return IntervalCache.create();
  }
}
