package gms.shared.cacheservice;


import gms.shared.frameworks.cache.utils.IgniteConnectionManager;
import gms.shared.frameworks.systemconfig.SystemConfig;
import org.apache.ignite.visor.commands.VisorConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class CacheService {
  private static final Logger logger = LoggerFactory.getLogger(CacheService.class);

  public static void main(String[] args) {
    // retVal exists solely to support unit testing
    boolean runVisor = CacheService.parseArgs(args);

    if (runVisor) {
      VisorConsole.main(args);
    } else {
      SystemConfig config = SystemConfig.create("cache-service");
      IgniteConnectionManager.initialize(config, new ArrayList<>());
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        logger.info("ignite closing");
        IgniteConnectionManager.close();
      }));
    }
  }

  protected static boolean parseArgs(String[] args) {
    return (args != null) && (args.length > 0) && "visor".equals(args[0]);
  }

}

