package gms.shared.frameworks.osd.coi.signaldetection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.apache.commons.lang3.Validate;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.stream.Stream;

@AutoValue
public abstract class StationGroup {

  public abstract String getName();

  public abstract String getDescription();

  public abstract NavigableSet<Station> getStations();

  public Stream<Station> stations() {
    return getStations().stream();
  }

  @JsonCreator
  public static StationGroup from(
    @JsonProperty("name") String name,
    @JsonProperty("description") String description,
    @JsonProperty("stations") List<Station> stations) {
    Validate.notEmpty(name);
    Validate.notEmpty(description);
    Validate.notEmpty(stations);

    NavigableSet<Station> stationSet = new TreeSet<>(Comparator.comparing(Station::getName));
    stationSet.addAll(stations);

    return new AutoValue_StationGroup(name, description, Collections.unmodifiableNavigableSet(stationSet));
  }
}
