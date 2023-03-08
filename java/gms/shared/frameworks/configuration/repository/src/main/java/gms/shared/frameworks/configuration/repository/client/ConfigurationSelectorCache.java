package gms.shared.frameworks.configuration.repository.client;

import gms.shared.frameworks.configuration.Selector;

import java.util.List;
import java.util.Map;

public interface ConfigurationSelectorCache {

  Map<String, Object> resolveFieldMap(List<Selector> selectors);
}
