package gms.testtools.simulators.bridgeddatasourceanalysissimulator;

import java.util.HashMap;
import java.util.Map;

public class IdentifierMapping {

  private long mappingOffset;
  private final Map<Long, Long> mapping;

  public IdentifierMapping(long mappingOffset) {
    this.mappingOffset = mappingOffset;
    this.mapping = new HashMap<>();
  }

  public void clear() {
    mapping.clear();
  }

  public long get(long key) {
    return mapping.computeIfAbsent(key, k -> mappingOffset++);
  }
}
