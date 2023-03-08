package gms.shared.frameworks.configuration.repository;

import gms.shared.frameworks.configuration.Configuration;
import gms.shared.frameworks.configuration.ConfigurationOption;
import gms.shared.frameworks.configuration.Constraint;
import gms.shared.frameworks.configuration.Operator;
import gms.shared.frameworks.configuration.Selector;
import gms.shared.frameworks.configuration.constraints.NumericScalarConstraint;
import gms.shared.frameworks.configuration.constraints.StringConstraint;
import gms.shared.frameworks.configuration.constraints.WildcardConstraint;
import gms.shared.frameworks.osd.coi.FieldMapUtilities;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConfigurationTestFixtures {

  private ConfigurationTestFixtures() {
  }

  public static final String configurationKey = "[component-name]-configuration";
  public static final String filterDefKey = "global.filter-definition";
  public static final String filterDescKey = "global.filter-description";
  public static final FooParameters fooParamsDefaults = FooParameters.from(100, "string100", true);
  public static final Map<String, Object> fooParamsDefaultsMap = FieldMapUtilities
    .toFieldMap(fooParamsDefaults);

  public static final NumericScalarConstraint snrIs5 = NumericScalarConstraint
    .from("snr", Operator.from(Operator.Type.EQ, false), 5.0, 100);

  // filter definition constraint
  private static final Constraint filterDefConstraint = StringConstraint
    .from("name", Operator.from(Operator.Type.EQ, false), Set.of("HAM FIR BP 0.40-3.50 Hz"), 100);

  private static final Constraint filterDescConstraint = StringConstraint
    .from("name", Operator.from(Operator.Type.EQ, false), Set.of("HAM FIR BP 0.40-3.50 Hz Description"), 100);

  // nested filter description selector
  private static final Selector filterDescSelector = Selector.from("name",
    "HAM FIR BP 0.40-3.50 Hz Description");

  // nested filter definition reference
  private static final Map<String, Object> nestedFilterRef = Map.of("$ref=global.filter-description",
    List.of(Map.of(
      "criterion", "name",
      "value", "HAM FIR BP 0.40-3.50 Hz Description"
      )));

  // filter definition param map
  private static final Map<String, Object> filterDefParams = Map.of(
    "name", "HAM FIR BP 0.40-3.50 Hz",
    "comments", "Filter 4 comments",
    "filterDescription", nestedFilterRef
    );

  // filter description nested params
  private static final Map<String, Object> filterDescNestedParams = Map.of(
    "sampleRateHz", 20.0,
    "sampleRateToleranceHz", 0.05,
    "aCoefficients", List.of(1.0),
    "bCoefficients", List.of(0.0008771884219456755),
    "groupDelaySec", "PT1.2S"
  );

  // filter description params map
  private static final Map<String, Object> filterDescParams = Map.of(
    "type", "LinearFilterDescription",
    "comments", "0.4 3.5 3 BP causal",
    "causal", true,
    "filterType", "FIR_HAMMING",
    "lowFrequency", 0.4,
    "highFrequency", 3.5,
    "order", 48,
    "zeroPhase", false,
    "passBandType", "BAND_PASS",
    "parameters", filterDescNestedParams);

  public static final ConfigurationOption configOptDefault = ConfigurationOption
    .from("SNR-5", List.of(WildcardConstraint.from("snr")), fooParamsDefaultsMap);

  public static final ConfigurationOption configOptSnrIs5 = ConfigurationOption
    .from("SNR-5", List.of(snrIs5), Map.of("a", 10));

  // filter definition config option
  private static final ConfigurationOption nestedConfigOption = ConfigurationOption
    .from("ham-fir-bp-0.40-3.50-Hz", List.of(filterDefConstraint), filterDefParams);

  // filter description config option
  private static final ConfigurationOption filterDescConfigOption = ConfigurationOption
    .from("ham-fir-bp-0.40-3.50-hz-description", List.of(filterDescConstraint), filterDescParams);

  public static final Configuration configurationSnrIs5 = Configuration
    .from(configurationKey, List.of(configOptDefault, configOptSnrIs5));

  // filter definition nested config
  public static final Configuration filterDefNestedConfig = Configuration
    .from(filterDefKey, List.of(nestedConfigOption));

  // filter description config
  public static final Configuration filterDescConfig = Configuration
    .from(filterDescKey, List.of(filterDescConfigOption));
}
