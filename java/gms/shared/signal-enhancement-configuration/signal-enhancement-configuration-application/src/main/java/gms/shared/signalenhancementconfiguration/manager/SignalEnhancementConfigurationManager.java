package gms.shared.signalenhancementconfiguration.manager;

import gms.shared.signalenhancementconfiguration.coi.filter.FilterListDefinition;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/signal-enhancement-configuration",
  produces = MediaType.APPLICATION_JSON_VALUE)
public class SignalEnhancementConfigurationManager {
  private final FilterListDefinition filterListDefinition;

  @Autowired
  public SignalEnhancementConfigurationManager(FilterListDefinition filterListDefinition) {
    this.filterListDefinition = filterListDefinition;
  }

  /**
   * Finds {@link FilterListDefinition} and returns serialized json response
   */
  @GetMapping(value = "/filter-lists-definition")
  @Operation(summary = "retrieves filter lists definition")
  public FilterListDefinition findFilterListsDefinition() {
    return filterListDefinition;
  }
}
