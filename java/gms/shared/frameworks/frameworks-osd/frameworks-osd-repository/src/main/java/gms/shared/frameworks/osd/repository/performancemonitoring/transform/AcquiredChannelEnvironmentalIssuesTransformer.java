package gms.shared.frameworks.osd.repository.performancemonitoring.transform;

import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueBoolean;
import gms.shared.frameworks.osd.dto.soh.DataPoint;
import gms.shared.frameworks.osd.dto.soh.DoubleOrInteger;
import gms.shared.frameworks.osd.dto.soh.HistoricalAcquiredChannelEnvironmentalIssues;
import gms.shared.frameworks.osd.dto.soh.LineSegment;
import gms.shared.frameworks.osd.dto.soh.LineSegment.Builder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

public class AcquiredChannelEnvironmentalIssuesTransformer {

  private AcquiredChannelEnvironmentalIssuesTransformer() {

  }

  public static List<HistoricalAcquiredChannelEnvironmentalIssues> toHistoricalAcquiredChannelEnvironmentalIssues(
    List<? extends AcquiredChannelEnvironmentIssue<?>> acquiredChannelEnvironmentIssues) {
    var historicalIssues = new ArrayList<HistoricalAcquiredChannelEnvironmentalIssues>();
    var channelIssueMap = new HashMap<String, List<LineSegment.Builder>>();
    if (acquiredChannelEnvironmentIssues.isEmpty()) {
      return historicalIssues;
    }
    var requestIssueTypes = acquiredChannelEnvironmentIssues
      .stream()
      .map(AcquiredChannelEnvironmentIssue::getType)
      .distinct()
      .collect(toList());
    checkState(requestIssueTypes.size() == 1,
      "Please request AcquiredChannelEnvironmentalIssues of one AcquiredChannelEnvironmentIssueType");
    var requestIssueType = requestIssueTypes.get(0);
    var channelAceiMap = acquiredChannelEnvironmentIssues.stream()
      .collect(Collectors
        .groupingByConcurrent(AcquiredChannelEnvironmentIssue::getChannelName,
          mapping(identity(), toList())));

    channelAceiMap.forEach((channelName, issues) -> {
      issues.sort(Comparator.comparing(AcquiredChannelEnvironmentIssue::getStartTime));
      channelIssueMap.computeIfAbsent(channelName, k -> {
        List<Builder> issuesList = new ArrayList<>();
        issuesList.add(LineSegment.builder());
        return issuesList;
      });
      var issuesListPairs = channelIssueMap.get(channelName);
      for (var i = 0; i < issues.size(); i++) {
        var issue = issues.get(i);
        var issuesListPairsIndex = issuesListPairs.size() - 1;
        issuesListPairs.get(issuesListPairsIndex).addDataPoint(populateIssues(issue, false));
        if (i == issues.size() - 1) {
          issuesListPairs.get(issuesListPairsIndex).addDataPoint(populateIssues(issue, true));
        } else {
          if (!issues.get(i + 1).getStartTime().equals(issue.getEndTime())) {
            issuesListPairs.get(issuesListPairsIndex)
              .addDataPoint(populateIssues(issue, true));
            channelIssueMap.get(channelName).add(LineSegment.builder());
          }
        }
      }
      historicalIssues.add(HistoricalAcquiredChannelEnvironmentalIssues.builder()
        .setChannelName(channelName)
        .setMonitorType(requestIssueType.name())
        .setTrendLine(
          channelIssueMap.get(channelName).stream().map(Builder::build).collect(toList()))
        .build());
    });

    return historicalIssues;
  }

  private static DataPoint populateIssues(AcquiredChannelEnvironmentIssue<?> issue,
    boolean getEndTime) {
    var issueTime = (getEndTime ? issue.getEndTime() : issue.getStartTime()).toEpochMilli();
    var historicalDataPointBuilder = DataPoint.builder()
      .setTimeStamp(issueTime);
    if (issue instanceof AcquiredChannelEnvironmentIssueBoolean) {
      historicalDataPointBuilder
        .setStatus(DoubleOrInteger.ofInteger(((boolean) issue.getStatus() ? 1 : 0)));
    } else {
      historicalDataPointBuilder
        .setStatus(DoubleOrInteger.ofDouble((double) issue.getStatus()));
    }

    return historicalDataPointBuilder.build();
  }

}
