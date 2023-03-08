package gms.shared.plugin;

import gms.shared.plugin.fixtures.BarImplOne;
import gms.shared.plugin.fixtures.BarImplTwo;
import gms.shared.plugin.fixtures.FooImplOne;
import gms.shared.plugin.fixtures.FooImplTwo;
import gms.shared.plugin.fixtures.IBar;
import gms.shared.plugin.fixtures.IFoo;
import gms.shared.plugin.injected.BazImplOne;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = {PluginRegistry.class, Plugin.class, BarImplOne.class, BarImplTwo.class,
  FooImplOne.class, FooImplTwo.class, BazImplOne.class})
@TestInstance(Lifecycle.PER_CLASS)
class PluginRegistryTest {

  private static final Logger logger = LoggerFactory.getLogger(PluginRegistryTest.class);

  @Autowired
  PluginRegistry registry;

  @Test
  void getPlugin() {

    //Verify that a valid and invalid do not throw

    Plugin foo = registry.getPlugin("fooImplOne", IFoo.class).get();
    Optional<IBar> barNull = registry.getPlugin("unImplementedInterface", IBar.class);

    assertDoesNotThrow(() -> registry.getPlugin("fooImplOne", IFoo.class));
    assertDoesNotThrow(() -> registry.getPlugin("unImplementedInterface", IFoo.class));

    assertNotNull(foo);
    Assertions.assertTrue(barNull.isEmpty(),
      "Map Failure- Passed in un-mapped case but received entry");

    logger.info("Retrieved plugin {} when using name fooImplOne to lookup in registry",
      foo.getName());

  }
}