package gms.tools.stationrefbuilder;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

/**
 * The main class for determining the station groups for the current station being considered.
 */
public class StationGroupBuilder {

  private static final Logger logger = LoggerFactory.getLogger(StationGroupBuilder.class);

  private StationGroupBuilderConfiguration stationGroupBuilderConfiguration;

  private URL scanDir;

  public StationGroupBuilder(StationGroupBuilderConfiguration conf, URL meta) {
    stationGroupBuilderConfiguration = conf;
    scanDir = meta;
  }

  /**
   * Check the current station passed in against all defined rules.
   *
   * @param station
   * @return a map of all rules with true as value if the station should be in that group,
   * false otherwise.
   */

  public Map<String, Boolean> checkAgainstAllRules(Station station) {
    Map<String, Boolean> groupsApplied = new HashMap<>();
    ArrayList<String> groupList = new ArrayList<>(stationGroupBuilderConfiguration.getGroupNameList());
    for (var i = 0; i < groupList.size(); i++) {
      var isInRule = false;
      var groupName = groupList.get(i);
      Optional<String> val = stationGroupBuilderConfiguration.getGroup(groupName).getLogic();
      String logic = val.orElse("");
      ArrayList<String> ruleList = new ArrayList<>(
        stationGroupBuilderConfiguration.getGroup(groupName).getRuleNameList());
      if ("COMPOUND".equals(logic)) {
        isInRule = getCompoundRule(station, groupName, ruleList);
      } else {
        isInRule = getSingleRule(station, groupName, ruleList, logic);
      }
      groupsApplied.put(groupName, isInRule);

    }
    return groupsApplied;
  }

  private boolean getSingleRule(Station station, String groupName, List<String> ruleList, String logic) {
    var isInRule = false;
    for (var j = 0; j < ruleList.size(); j++) {
      var rule = stationGroupBuilderConfiguration.getGroup(groupName).getRule(ruleList.get(j));
      isInRule = checkForRule(rule, station);

      if (ruleList.size() > 1 && "".equals(logic)) {
        logger.error(
          "For more than one rule, a logic operator has to be set to either COMPOUND, OR or AND.");
      } else if ((isInRule && "OR".equals(logic)) ||
        (!isInRule && "AND".equals(logic))) {
        break;
      }
    }
    return isInRule;
  }

  /**
   * Compounded rules are resolved here... expected is "OR" at the highest level
   * and "AND" for each sublevel... LookupTable name determines WHICH rules apply to the AND, they
   * have to match.
   *
   * @param station The station to test
   * @param groupName The name of the group to assign station to if rule applies
   * @param ruleList The list of rules (more than one)
   * @return true if station should be in the groupName.
   */
  private boolean getCompoundRule(Station station, String groupName, List<String> ruleList) {
    HashMap<String, List<String>> compoundRules = new HashMap<>();
    var isInRule = false;
    for (var x = 0; x < ruleList.size(); x++) {
      List<String> tmpList;
      String lookup = stationGroupBuilderConfiguration.getGroup(groupName).getRule(ruleList.get(x))
        .getIndex().orElse("");
      if (compoundRules.containsKey(lookup)) {
        tmpList = compoundRules.get(lookup);
      } else {
        tmpList = new ArrayList<>();
      }
      tmpList.add(ruleList.get(x));
      compoundRules.put(lookup, tmpList);
    }

    for (Map.Entry<String, List<String>> entry : compoundRules.entrySet()) {
      List<String> values = entry.getValue();
      var tmpIsInRule = false;
      for (String ruleChosen : values) {
        var rule = stationGroupBuilderConfiguration.getGroup(groupName).getRule(ruleChosen);
        tmpIsInRule = checkForRule(rule, station);
        if (!tmpIsInRule) {
          //done with checking this set of rules... does not apply. Move on to next set...
          break;
        }

      }
      if (tmpIsInRule) {
        //it's an OR, at this level, so if it makes it here, return true...
        isInRule = true;
        break;
      }
    }
    return isInRule;
  }

  /**
   * This method checks a single station against a single rule...
   *
   * @param rule The rule to be examined
   * @param station For the station in question
   * @return true if the rule evaluates to true, false otherwise or if an error occurred.
   */
  private boolean checkForRule(Rule rule, Station station) {
    String ruleType = rule.getType();
    var result = false;
    if ("none".equals(ruleType)) {
      //Base Case: There are no rules, always add station to the group!
      result = true;
    } else if ("alphabet".equals(ruleType)) {
      result = checkAlphabetRule(rule, station);
    } else if ("lookup".equals(ruleType)) {
      result = checkLookupRule(rule, station);
    } else if ("find".equals(ruleType)) {
      result = checkFindRule(rule, station);
    } else if ("match".equals(ruleType)) {
      result = checkMatchRule(rule, station);
    } else if ("negate".equals(ruleType)) {
      Optional<String> val = rule.getValue();
      String value = val.orElse("");
      var tmpRule = stationGroupBuilderConfiguration.getGroup(value).getRule(rule.getName());
      return !checkForRule(tmpRule, station);
    }
    return result;
  }

  /**
   * Rule type match is resolved here.
   * Takes any string and matches it in file annotated in the rule. If a match is found, return true.
   */
  private boolean checkMatchRule(Rule rule, Station station) {
    var result = false;
    String table = rule.getTable().orElse("");
    String value = rule.getValue().orElse("");
    var fileName = String.format("%s/%s/%s", scanDir.getPath(), station.getStationName(), table);
    var checkFile = new File(fileName);
    try (var scanner = new Scanner(checkFile)) {
      while (scanner.hasNextLine()) {
        String matchLine = scanner.nextLine();
        if (matchLine.contains(value)) {
          result = true;
          break;
        }
      }

    } catch (Exception e) {
      logger.error("Exception occurred for Rule: {}. Exception: {}", rule, e.toString());
      result = false;
    }
    return result;
  }

  /**
   * Rule type 'alphabet' is resolved here...
   *
   * @param rule
   * @param station
   * @return true if rule is met
   */
  private boolean checkAlphabetRule(Rule rule, Station station) {
    var result = false;
    try {
      String table = rule.getTable().orElse("");
      String field = rule.getField().orElse("");
      Class<?> c = Class.forName(this.getClass().getPackageName() + "." + table);
      var method = c.getDeclaredMethod("get" + field);
      String operator = rule.getOperator().orElse("");
      String value = rule.getValue().orElse("");
      if (method.getGenericReturnType().toString().contains("java.util.List")) {
        List<Channel> tmp = (List<Channel>) (method.invoke(station, null));
        var counter = 0;
        while (!result && counter < tmp.size()) {
          String chan = tmp.get(counter++).getChannelName();
          chan = chan.substring(chan.lastIndexOf('.') + 1);
          result = checkForOperator(chan, operator, value);
        }
      } else {
        var tmpResult = String.valueOf(method.invoke(station, null));
        result = checkForOperator(tmpResult, operator, value);
      }
    } catch (Exception e) {
      //error... obviously something is wrong. So log it, and return false.
      logger.error("Error: checkForRule for ALPHABET rule returned an error for Rule: {}. Check formatting of rule. Exception: {}"
        , rule, e.getMessage());
      result = false;
    }
    return result;
  }

  private boolean checkForOperator(String tmp, String operator, String value) {
    var result = false;
    if (("<".equals(operator) &&
      (tmp.charAt(0) < value.charAt(0))) ||
      ("==".equals(operator) &&
        (tmp.startsWith(value))) ||
      (">".equals(operator) &&
        (tmp.charAt(0) > value.charAt(0)))) {
      result = true;
    }

    return result;
  }

  /**
   * since 'lookup' type rules can be complex, they are resolved here, or further simplified to then
   * call checkLookupTableSingleValue, which is the default case...
   *
   * @param rule
   * @param station
   * @return true if rule is met
   */
  private boolean checkLookupRule(Rule rule, Station station) {
    var result = false;
    String table = rule.getTable().orElse("");

    String field = rule.getField().orElse("");
    String operator = rule.getOperator().orElse("");
    try {
      Class<?> c = Class.forName(this.getClass().getPackageName() + "." + table);
      var method = c.getDeclaredMethod("get" + field);
      if (!"".equals(operator)) {
        //This means there is another sublevel defined here...
        List<Channel> tmpResult = (List<Channel>) (method.invoke(station, null));
        var counter = 0;
        while (!result && counter < tmpResult.size()) {
          String chan = tmpResult.get(counter).getChannelName();
          //because there is a chance of having station name match a channel name, strip station name.
          chan = chan.substring(chan.lastIndexOf('.'));
          result = checkLookupTableSingleValue(rule, chan);
          counter++;
        }
      } else {  //single value
        var tmpResult = String.valueOf(method.invoke(station, null));
        //do the enum thing...
        result = checkLookupTableSingleValue(rule, tmpResult);
      }
    } catch (Exception e) {
      //error... log and return false...
      logger.error(
        "Error: checkForRule for LOOKUP rule returned an error for Rule: {}. Check formatting of rule. Exception: {}",
        rule, e.getMessage());
      result = false;
    }
    return result;
  }

  /**
   * Rule type of 'lookup' is further resolved here by worrying only about a single value. This is
   * the default case.
   *
   * @param rule
   * @param tmpResult
   * @return true if rule is met
   */
  private boolean checkLookupTableSingleValue(Rule rule, String tmpResult) {
    String lookupTable = rule.getLookuptable().orElse("");
    String value = rule.getValue().orElse("");
    var result = false;
    try {
      Class<?> lookup = Class.forName(this.getClass().getPackageName() + "." + lookupTable);
      List<?> lookupList = Arrays.asList(lookup.getEnumConstants());
      var enumConstant = lookup.getField(value.toUpperCase());

      for (var i = 0; i < lookupList.size(); i++) {
        String[] match = (String[]) lookup.getDeclaredMethod("getValuesByEnum", lookup)
          .invoke(lookup, lookupList.get(i));
        for (var j = 0; j < match.length; j++) {
          if (tmpResult.contains(match[j]) && lookupList.get(i).toString()
            .equals(enumConstant.getName())) {
            //matched it!
            result = true;
          }
        }
      }
    } catch (Exception e) {
      logger.error("A problem occurred matching this value {} to the rule {}. Exception {}",
        tmpResult, rule, e.getMessage());
      result = false;
    }
    return result;
  }

  /**
   * Rule type of 'find' is resolved here.
   *
   * @param rule
   * @param station
   * @return true if rule is met
   */
  private boolean checkFindRule(Rule rule, Station station) {
    var result = false;
    try {
      String table = rule.getTable().orElse("");
      String field = rule.getField().orElse("");
      String value = rule.getValue().orElse("");

      Class<?> c = Class.forName(this.getClass().getPackageName() + "." + table);
      var method = c.getDeclaredMethod("get" + field);
      var tmpResult = String.valueOf(method.invoke(station, null));

      if (tmpResult.equalsIgnoreCase(value)) {
        result = true;
      }

    } catch (Exception e) {
      //error... obviously something is wrong. So log it, and return false.
      logger.error("Error: checkForRule for ALPHABET rule returned an error for Rule: {}. Check formatting of rule. Exception: {}"
        , rule, e.getMessage());
      result = false;
    }
    return result;
  }


}
