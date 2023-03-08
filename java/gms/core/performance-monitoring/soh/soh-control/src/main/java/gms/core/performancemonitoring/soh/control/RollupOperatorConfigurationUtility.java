package gms.core.performancemonitoring.soh.control;

import gms.core.performancemonitoring.soh.control.capabilityrollup.RollupOperatorType;
import gms.core.performancemonitoring.soh.control.configuration.BestOfRollupOperator;
import gms.core.performancemonitoring.soh.control.configuration.MinGoodOfRollupOperator;
import gms.core.performancemonitoring.soh.control.configuration.RollupOperator;
import gms.core.performancemonitoring.soh.control.configuration.WorstOfRollupOperator;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import org.apache.commons.lang3.Validate;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for deserializing RollupOperator based on these needs:
 * <ul>
 * <li>
 *   For a given operator for a given operand type, a missing list of operands implies that the
 *   list of operands should come from a seperate source. (See CapabilityRollupConfigurationUtility -
 *   this utility does not concern itself with that source.)
 * </li>
 * <li>
 *   For a given operator for a given operand type, the operand lists for other operand types are all
 *   missing.
 * </li>
 * </ul>
 */
class RollupOperatorConfigurationUtility {

  private static final String OPERATOR_TYPE_KEY = "operatorType";
  private static final String GOOD_THRESHOLD_KEY = "goodThreshold";
  private static final String MARGINAL_THRESHOLD_KEY = "marginalThreshold";
  private static final String STATION_OPERANDS_KEY = "stationOperands";
  private static final String CHANNEL_OPERANDS_KEY = "channelOperands";
  private static final String SOH_MONITOR_TYPE_OPERANDS_KEY = "sohMonitorTypeOperands";
  private static final String ROLLUP_OPERATOR_OPERANDS_KEY = "rollupOperatorOperands";

  private static final Set<String> GENERAL_OPERAND_FIELDS = Set.of(
    STATION_OPERANDS_KEY,
    CHANNEL_OPERANDS_KEY,
    SOH_MONITOR_TYPE_OPERANDS_KEY,
    ROLLUP_OPERATOR_OPERANDS_KEY
  );

  private static final Set<String> ALL_POSSIBLE_FIELDS = Set.of(
    OPERATOR_TYPE_KEY,
    GOOD_THRESHOLD_KEY,
    MARGINAL_THRESHOLD_KEY,
    STATION_OPERANDS_KEY,
    CHANNEL_OPERANDS_KEY,
    SOH_MONITOR_TYPE_OPERANDS_KEY,
    ROLLUP_OPERATOR_OPERANDS_KEY
  );

  private static final Set<String> MIN_GOOD_OF_FIELDS = Set.of(
    GOOD_THRESHOLD_KEY,
    MARGINAL_THRESHOLD_KEY
  );

  /**
   * Enum for the different types of operands.
   */
  enum TerminalRollupOperatorResolver {
    //
    // This enum allows us to not have a second level of switches/if-elses for the operand type.
    // There is already one level of these for the operator type.
    //
    STATION_OPERATOR_RESOLVER {
      @Override
      <T> RollupOperator resolve(Map<String, ?> rollupOperatorObjectMap, List<T> ifEmptyList) {
        return RollupOperatorConfigurationUtility.resolveTerminalStationOperatorFromObjectMap(
          rollupOperatorObjectMap,
          ifEmptyList
        );
      }
    },

    CHANNEL_OPERATOR_RESOLVER {
      @Override
      <T> RollupOperator resolve(Map<String, ?> rollupOperatorObjectMap, List<T> ifEmptyList) {
        return RollupOperatorConfigurationUtility.resolveTerminalChannelOperatorFromObjectMap(
          rollupOperatorObjectMap,
          ifEmptyList
        );
      }
    },

    SOH_MONITOR_TYPE_OPERATOR_RESOLVER {
      @Override
      <T> RollupOperator resolve(Map<String, ?> rollupOperatorObjectMap, List<T> ifEmptyList) {
        return RollupOperatorConfigurationUtility.resolveTerminalMonitorTypeOperatorFromObjectMap(
          rollupOperatorObjectMap,
          ifEmptyList
        );
      }
    };

    /**
     * Resolve a terminal rollup operator
     *
     * @param rollupOperatorObjectMap object map to resolve
     * @param ifEmptyList list to use if the respective operand list is empty
     * @return a terminal rollup operator.
     */
    abstract <T> RollupOperator resolve(Map<String, ?> rollupOperatorObjectMap,
      List<T> ifEmptyList);

  }

  /**
   * Creates a rollup operator using the provided key (String) -> object map. It is a map of field
   * name to value.
   *
   * @param rollupOperatorObjectMap the key -> object map
   * @param ifEmptyList Populate empty operand lists with this
   * @param terminalRollupOperatorResolver The resolver to use for the terminal operators.
   * @param <T> Type of operands. Depends on rollupOperandType
   * @return A new rollup operator.
   */
  @SuppressWarnings("unchecked")
  static <T> RollupOperator resolveOperator(
    Map<String, ?> rollupOperatorObjectMap,
    List<T> ifEmptyList,
    TerminalRollupOperatorResolver terminalRollupOperatorResolver
  ) {

    var nestedRollupOperatorsList =
      Optional.ofNullable(rollupOperatorObjectMap.get(ROLLUP_OPERATOR_OPERANDS_KEY))
        .map(o -> {

          //
          // "unchecked" suppression:
          // Either this is Collection of Map<String, ?>, or something is terribly wrong. Thus
          // we are making a strong assumption that it is, because that is what the
          // config resolver gives us.
          //
          var roObjectMapCollection = (Collection<Map<String, ?>>) o;

          return roObjectMapCollection.stream().map(o1 ->
            resolveOperator(
              o1,
              ifEmptyList,
              terminalRollupOperatorResolver
            )
          ).collect(Collectors.toList());
        }).orElse(List.of());

    if (nestedRollupOperatorsList.isEmpty()) {
      //
      // Call the resolver, to resolve a terminal operator, allowing us to be agnostic of
      // what the operator operates on, avoiding ugly switches and duplicated code.
      //
      return terminalRollupOperatorResolver.resolve(
        rollupOperatorObjectMap, ifEmptyList
      );
    } else {
      //
      // Here if we see a non-empty list of nested rollup operators, we are going to assume that
      // this is truth and check that there are no other operands.
      //
      checkFieldExclusivity(
        rollupOperatorObjectMap,
        ROLLUP_OPERATOR_OPERANDS_KEY
      );

      //
      // Generate a non-terminal operator.
      //
      var rollupOperatorType = getRollupOperatorType(rollupOperatorObjectMap);

      if (rollupOperatorType == RollupOperatorType.BEST_OF) {
        return BestOfRollupOperator.from(
          List.of(),
          List.of(),
          List.of(),
          nestedRollupOperatorsList
        );
      } else if (rollupOperatorType == RollupOperatorType.WORST_OF) {
        return WorstOfRollupOperator.from(
          List.of(),
          List.of(),
          List.of(),
          nestedRollupOperatorsList
        );
      } else if (rollupOperatorType == RollupOperatorType.MIN_GOOD_OF) {
        return MinGoodOfRollupOperator.from(
          List.of(),
          List.of(),
          List.of(),
          nestedRollupOperatorsList,
          (Integer) rollupOperatorObjectMap.get(GOOD_THRESHOLD_KEY),
          (Integer) rollupOperatorObjectMap.get(MARGINAL_THRESHOLD_KEY)
        );
      }

      throw new IllegalArgumentException("resolveOperator: Unknown operator type");
    }
  }

  /**
   * Generates a terminal rollup operator that operates on stations from the rollupOperatorObjectMap.
   *
   * @param ifEmptyList Populate empty operand list with this.
   * @param <T> This will always be String, but we want a type parameter here so that
   * RollupOperandType.getTerminalRollupOperatorResolver can return this method reference.
   * @return terminal rollup operator that operates on stations.
   */
  private static <T> RollupOperator resolveTerminalStationOperatorFromObjectMap(
    Map<String, ?> rollupOperatorObjectMap,
    List<T> ifEmptyList
  ) {

    return resolveTerminalStationOrChannelRollupOperatorFromObjectMap(
      rollupOperatorObjectMap,
      STATION_OPERANDS_KEY,
      ifEmptyList.stream().map(Object::toString).collect(Collectors.toList())
    );
  }

  /**
   * Generates a terminal rollup operator that operates on channels from the rollupOperatorObjectMap.
   *
   * @param ifEmptyList Populate empty operand list with this.
   * @param <T> This will always be String, but we want a type parameter here so that
   * RollupOperandType.getTerminalRollupOperatorResolver can return this method reference.
   * @return terminal rollup operator that operates on channels.
   */
  private static <T> RollupOperator resolveTerminalChannelOperatorFromObjectMap(
    Map<String, ?> rollupOperatorObjectMap,
    List<T> ifEmptyList
  ) {

    return resolveTerminalStationOrChannelRollupOperatorFromObjectMap(
      rollupOperatorObjectMap,
      CHANNEL_OPERANDS_KEY,
      ifEmptyList.stream().map(Object::toString).collect(Collectors.toList())
    );
  }

  /**
   * Generates a terminal rollup operator that operates on SohMonitorTypes from the rollupOperatorObjectMap.
   *
   * @param ifEmptyList Populate empty operand list with this.
   * @param <T> This will always be SohMonitorType, but we want a type parameter here so that
   * RollupOperandType.getTerminalRollupOperatorResolver can return this method reference.
   * @return terminal rollup operator that operates on SohMonitorTypes.
   */
  private static <T> RollupOperator resolveTerminalMonitorTypeOperatorFromObjectMap(
    Map<String, ?> rollupOperatorObjectMap,
    List<T> ifEmptyList
  ) {

    var rollupOperatorType = getRollupOperatorType(rollupOperatorObjectMap);

    List<SohMonitorType> operands = resolveSohMonitorTypeOperandsFromObjectMap(
      rollupOperatorObjectMap,
      SOH_MONITOR_TYPE_OPERANDS_KEY,
      ifEmptyList.stream().map(o -> (SohMonitorType) o).collect(Collectors.toList())
    );

    if (rollupOperatorType == RollupOperatorType.BEST_OF) {
      return BestOfRollupOperator.from(
        List.of(),
        List.of(),
        operands,
        List.of()
      );
    } else if (rollupOperatorType == RollupOperatorType.WORST_OF) {
      return WorstOfRollupOperator.from(
        List.of(),
        List.of(),
        operands,
        List.of()
      );
    } else if (rollupOperatorType == RollupOperatorType.MIN_GOOD_OF) {
      return MinGoodOfRollupOperator.from(
        List.of(),
        List.of(),
        operands,
        List.of(),
        (Integer) rollupOperatorObjectMap.get(GOOD_THRESHOLD_KEY),
        (Integer) rollupOperatorObjectMap.get(MARGINAL_THRESHOLD_KEY)
      );
    }

    throw new IllegalArgumentException(
      "resolveTerminalMonitorTypeOperatorFromObjectMap: Unknown operator type");
  }

  /**
   * Generates a terminal rollup operator that operates on channels or stations from the rollupOperatorObjectMap.
   *
   * @param fieldName key that points to the list of station or channels names in the object map.
   * @param ifEmptyList Use this list in place of empty station or channel operands.
   * @return A RollupOperator that operates on channels or stations.
   */
  private static RollupOperator resolveTerminalStationOrChannelRollupOperatorFromObjectMap(
    Map<String, ?> rollupOperatorObjectMap,
    String fieldName,
    List<String> ifEmptyList
  ) {

    var rollupOperatorType = getRollupOperatorType(rollupOperatorObjectMap);

    List<String> operands = resolveStringOperandsFromObjectMap(
      rollupOperatorObjectMap,
      fieldName,
      ifEmptyList
    );

    if (rollupOperatorType == RollupOperatorType.BEST_OF) {
      return BestOfRollupOperator.from(
        STATION_OPERANDS_KEY.equals(fieldName) ? operands : List.of(),
        CHANNEL_OPERANDS_KEY.equals(fieldName) ? operands : List.of(),
        List.of(),
        List.of()
      );
    } else if (rollupOperatorType == RollupOperatorType.WORST_OF) {
      return WorstOfRollupOperator.from(
        STATION_OPERANDS_KEY.equals(fieldName) ? operands : List.of(),
        CHANNEL_OPERANDS_KEY.equals(fieldName) ? operands : List.of(),
        List.of(),
        List.of()
      );
    } else if (rollupOperatorType == RollupOperatorType.MIN_GOOD_OF) {
      return MinGoodOfRollupOperator.from(
        STATION_OPERANDS_KEY.equals(fieldName) ? operands : List.of(),
        CHANNEL_OPERANDS_KEY.equals(fieldName) ? operands : List.of(),
        List.of(),
        List.of(),
        (Integer) rollupOperatorObjectMap.get(GOOD_THRESHOLD_KEY),
        (Integer) rollupOperatorObjectMap.get(MARGINAL_THRESHOLD_KEY)
      );
    }

    throw new IllegalArgumentException(
      "resolveTerminalStationOrChannelRollupOperatorFromObjectMap: Unknown operator type");
  }

  /**
   * Resolve the list of String operands pointed to by the given key in the rollupOperatorObjectMap; if it is not
   * in the map, use ifEmptyList instead.
   */
  private static List<String> resolveStringOperandsFromObjectMap(
    Map<String, ?> rollupOperatorObjectMap,
    String fieldName,
    List<String> ifEmptyList
  ) {

    checkFields(
      fieldName,
      rollupOperatorObjectMap
    );

    return Optional.ofNullable(
      rollupOperatorObjectMap.get(fieldName)
    ).map(o -> {
      var rawList = (List<?>) o;

      return rawList.parallelStream().map(Object::toString).collect(Collectors.toList());
    }).orElse(ifEmptyList);
  }

  /**
   * Resolve the list of SohMonitorType operands pointed to by the given key in the rollupOperatorObjectMap; if
   * it is not in the map, use ifEmptyList instead.
   */
  private static List<SohMonitorType> resolveSohMonitorTypeOperandsFromObjectMap(
    Map<String, ?> rollupOperatorObjectMap,
    String fieldName,
    List<SohMonitorType> ifEmptyList
  ) {

    checkFields(
      fieldName,
      rollupOperatorObjectMap
    );

    return Optional.ofNullable(
      rollupOperatorObjectMap.get(fieldName)
    ).map(o -> {
      var rawList = (List<?>) o;

      return rawList.parallelStream().map(o1 -> SohMonitorType.valueOf(o1.toString()))
        .collect(Collectors.toList());
    }).orElse(ifEmptyList);
  }

  /**
   * Check that no other operand field that is not fieldName is in the object map.
   */
  private static void checkFieldExclusivity(Map<String, ?> rollupOperatorObjectMap,
    String fieldName) {

    rollupOperatorObjectMap.keySet().stream()
      .filter(otherFieldName -> !otherFieldName.equals(fieldName))
      .forEach(otherFieldName ->
        Validate.isTrue(
          !GENERAL_OPERAND_FIELDS.contains(otherFieldName),
          "Only %s can be specified for this RollupOperator, but %s was specified: %s",
          fieldName,
          otherFieldName,
          deterministicMapToString(rollupOperatorObjectMap)
        )
      );
  }

  /**
   * Check that there are no fields that are not part of any rollup operator.
   */
  private static void checkFieldsAreSubsetOfRequired(Map<String, ?> rollupOperatorObjectMap) {

    rollupOperatorObjectMap.keySet()
      .forEach(
        fieldName -> Validate.isTrue(ALL_POSSIBLE_FIELDS.contains(fieldName),
          "%s is not a valid field for rollup operators: %s",
          fieldName,
          deterministicMapToString(rollupOperatorObjectMap)
        )
      );
  }

  /**
   * Get the type of operator that the object map is trying to be.
   */
  private static RollupOperatorType getRollupOperatorType(Map<String, ?> rollupOperatorObjectMap) {

    return RollupOperatorType
      .valueOf(
        Optional.ofNullable(rollupOperatorObjectMap.get(OPERATOR_TYPE_KEY))
          .orElseThrow(
            () -> new IllegalArgumentException(
              "Missing operator type for this rollup operator: "
                + deterministicMapToString(rollupOperatorObjectMap)))
          .toString());
  }

  /**
   * Check the validity of the field and the validity of the fields in the object map.
   */
  private static void checkFields(String fieldName, Map<String, ?> rollupOperatorObjectMap) {

    checkFieldsAreSubsetOfRequired(
      rollupOperatorObjectMap
    );

    checkFieldExclusivity(
      rollupOperatorObjectMap,
      fieldName
    );

    if (getRollupOperatorType(rollupOperatorObjectMap) == RollupOperatorType.MIN_GOOD_OF) {
      MIN_GOOD_OF_FIELDS.forEach(requiredField ->
        Validate.isTrue(
          rollupOperatorObjectMap.containsKey(requiredField),
          "%s is a required field for MIN_GOOD_OF but was not found: %s",
          requiredField,
          deterministicMapToString(rollupOperatorObjectMap)
        )
      );
    } else {
      rollupOperatorObjectMap.keySet().forEach(fieldName1 ->
        Validate.isTrue(
          !MIN_GOOD_OF_FIELDS.contains(fieldName1),
          "%s is only valid for MIN_GOOD_OF: %s",
          fieldName1,
          deterministicMapToString(rollupOperatorObjectMap)
        )
      );
    }
  }

  /**
   * Convert a map to a string, where the entries are ordered deterministically. This is so that
   * we can actually test that an error message prints the right map.
   */
  private static String deterministicMapToString(Map<String, ?> objectMap) {

    return "{" +
      objectMap.keySet().stream()
        .sorted()
        .reduce(
          "",
          (previous, key) -> {
            String objectAsString;
            if (ROLLUP_OPERATOR_OPERANDS_KEY.equals(key)) {
              //
              // Don't bother recursing into this list of more maps (rollup operators).
              //
              // If we just print them out without running deterministicMapToString on them,
              // they'll create a non-deterministic string again.
              //
              objectAsString = "[...]";
            } else {
              objectAsString = objectMap.get(key).toString();
            }
            return previous + key + "=" + objectAsString + ", ";
          })
        //
        // Take out the last comma-space
        //
        .replaceAll(", $", "") + "}";
  }
}
