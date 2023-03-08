package gms.shared.frameworks.osd.dto.soh;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import java.util.Collection;

@AutoValue
public abstract class LineSegment {

  public abstract ImmutableList<DataPoint> getDataPoints();

  public static LineSegment.Builder builder() {
    return new AutoValue_LineSegment.Builder();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    abstract LineSegment.Builder setDataPoints(
      ImmutableList<DataPoint> dataPoints);

    public LineSegment.Builder setDataPoints(Collection<DataPoint> dataPoints) {
      return setDataPoints(ImmutableList.copyOf(dataPoints));
    }

    abstract ImmutableList.Builder<DataPoint> dataPointsBuilder();

    public LineSegment.Builder addDataPoint(DataPoint dataPoint) {
      dataPointsBuilder().add(dataPoint);
      return this;
    }

    abstract LineSegment autoBuild();

    public LineSegment build() {
      return autoBuild();
    }
  }

}
