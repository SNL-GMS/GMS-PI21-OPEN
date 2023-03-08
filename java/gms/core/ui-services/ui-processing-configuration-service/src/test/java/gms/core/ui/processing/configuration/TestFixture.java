package gms.core.ui.processing.configuration;

import gms.shared.frameworks.configuration.Selector;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class TestFixture {
  public static final ConfigQuery query = ConfigQuery.from(
    "a name", List.of(Selector.from("criteria", 5)));
  public static final Map<String, Object> result
    = Map.of("a", 1, "b", Instant.now());
}
