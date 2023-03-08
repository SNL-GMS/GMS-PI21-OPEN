package gms.shared.workflow.repository.util;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;

/**
 * Facilitates conversion between legacy and modern intervals
 */
public class IntervalUtility {

  public static final String AUTO_POST_AL_1 = "Auto Post-AL1";
  public static final String AL_1 = "AL1";
  public static final String AL_2 = "AL2";
  public static final String AUTO_NETWORK = "Auto Network";

  private static final Map<Pair<String, String>, String> legacyClassAndNameToStageName = Map.of(
    Pair.of("NET", "NETS1"), AUTO_NETWORK,
    Pair.of("ARS", AL_1), AL_1,
    Pair.of("AUTO", AL_1), AUTO_POST_AL_1,
    Pair.of("ARS", AL_2), AL_2
  );

  private static final Map<String, Pair<String, String>> stageNameToLegacyClassAndName = legacyClassAndNameToStageName
    .entrySet().stream()
    .collect(toMap(Map.Entry::getValue, Map.Entry::getKey));

  private IntervalUtility() {

  }

  /**
   * Converts a legacy stage class and name to a modern stage name
   *
   * @param legacyClass legacy stage class
   * @param legacyName legacy stage name
   * @return Modern stage name
   */
  public static Optional<String> getStageName(String legacyClass, String legacyName) {
    return Optional.ofNullable(legacyClassAndNameToStageName.get(Pair.of(legacyClass, legacyName)));
  }

  /**
   * Converts a modern stage name to a legacy stage class and name
   *
   * @param stageName Modern stage name to convert to legacy class and name
   * @return Legacy stage class and stage name
   */
  public static Optional<Pair<String, String>> getLegacyClassAndName(String stageName) {
    return Optional.ofNullable(stageNameToLegacyClassAndName.get(stageName));
  }

}
