package gms.shared.frameworks.osd.coi.signaldetection;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class StationGroupTests {

  @Test
  void testFactoryMethodWithNullStationNameThrowsException() {
    List<Station> stationList = List.of(Mockito.mock(Station.class));

    Exception e = assertThrows(NullPointerException.class,
      () -> StationGroup.from(null, "test", stationList));
  }

  @Test
  void testFactoryMethodWithEmptyNameThrowsException() {
    List<Station> stationList = List.of(Mockito.mock(Station.class));
    Exception e = assertThrows(IllegalArgumentException.class,
      () -> StationGroup.from("", "test", stationList));
  }

  @Test
  void testFactoryMethodWithNullDescriptionThrowsException() {
    List<Station> stationList = List.of(Mockito.mock(Station.class));
    Exception e = assertThrows(NullPointerException.class,
      () -> StationGroup.from("Test Station Group", null, stationList));
  }

  @Test
  void testFactoryMethodWithEmptyDescriptionThrowsException() {
    List<Station> stationList = List.of(Mockito.mock(Station.class));
    Exception e = assertThrows(IllegalArgumentException.class,
      () -> StationGroup.from("test", "", stationList));
  }

  @Test
  void testFactoryMethodWithNullStationListThrowsException() {
    Exception e = assertThrows(NullPointerException.class,
      () -> StationGroup.from("test", "test description", null));
  }

  @Test
  void testFactoryMethodWithEmptyStationListThrowsException() {
    List<Station> emptyList = List.of();
    Exception e = assertThrows(IllegalArgumentException.class,
      () -> StationGroup.from("test", "test description", emptyList));

  }
}
