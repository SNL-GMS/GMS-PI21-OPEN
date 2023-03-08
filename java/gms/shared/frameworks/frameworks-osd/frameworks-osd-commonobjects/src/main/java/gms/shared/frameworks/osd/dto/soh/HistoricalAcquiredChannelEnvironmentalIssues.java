package gms.shared.frameworks.osd.dto.soh;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import java.util.Collection;

/**
 * Encapsulates a view of historical environmental issues data acquired on a channel
 */
@AutoValue
@JsonSerialize(using = HistoricalAcquiredChannelEnvironmentalIssuesSerializer.class)
@JsonDeserialize(builder = AutoValue_HistoricalAcquiredChannelEnvironmentalIssues.Builder.class)
public abstract class HistoricalAcquiredChannelEnvironmentalIssues {

  public abstract String getChannelName();

  public abstract String getMonitorType();

  /**
   * Get the environmental issues trend line. The inner most collection represents a data point with
   * x value start time and y value status, either int or double. The exception to this is the last
   * data point; it is comprised of end time and status matching the status of the preceding data point.
   * In the below example, the trend line is comprised of three line segments. There is a gap in data
   * between each line segment.
   * <p>
   * serialized example:
   * [ [ [ 1482456217000, 0 ], [ 1482456293000, 1 ], [ 1482456369000, 0 ], [ 1482456445000, 0 ] ],
   * [ [ 1482457781000, 1 ], [ 1482457857000, 0 ], [ 1482457933000, 1 ], [ 1482458009000, 1 ] ],
   * [ [ 1482459345000, 0 ], [ 1482459421000, 1 ], [ 1482459497000, 0 ], [ 1482459573000, 0 ] ] ]
   *
   * @return {@link ImmutableList}{@literal <}{@link LineSegment}{@literal >} a collection of line segments that comprise the trend line
   */
  public abstract ImmutableList<LineSegment> getTrendLine();

  public static HistoricalAcquiredChannelEnvironmentalIssues.Builder builder() {
    return new AutoValue_HistoricalAcquiredChannelEnvironmentalIssues.Builder();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    public abstract HistoricalAcquiredChannelEnvironmentalIssues.Builder setChannelName(
      String name);

    public abstract HistoricalAcquiredChannelEnvironmentalIssues.Builder setMonitorType(
      String type);

    abstract HistoricalAcquiredChannelEnvironmentalIssues.Builder setTrendLine(
      ImmutableList<LineSegment> lineSegmentList);

    public HistoricalAcquiredChannelEnvironmentalIssues.Builder setTrendLine(
      Collection<LineSegment> lineSegmentList) {
      return setTrendLine(ImmutableList.copyOf(lineSegmentList));
    }

    abstract ImmutableList.Builder<LineSegment> trendLineBuilder();

    public HistoricalAcquiredChannelEnvironmentalIssues.Builder addLineSegment(
      LineSegment lineSegment) {
      trendLineBuilder().add(lineSegment);
      return this;
    }

    abstract HistoricalAcquiredChannelEnvironmentalIssues autoBuild();

    public HistoricalAcquiredChannelEnvironmentalIssues build() {
      return autoBuild();
    }
  }

}
