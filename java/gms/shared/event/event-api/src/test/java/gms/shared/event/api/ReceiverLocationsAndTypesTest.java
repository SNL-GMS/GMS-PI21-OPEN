package gms.shared.event.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.auto.value.AutoValue;
import gms.shared.stationdefinition.coi.channel.ChannelBandType;
import gms.shared.stationdefinition.coi.channel.ChannelDataType;
import gms.shared.stationdefinition.coi.channel.Location;
import gms.shared.utilities.test.TestUtilities;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@AutoValue
@JsonIgnoreProperties(ignoreUnknown = true)
class ReceiverLocationsAndTypesTest {

  @Test
  void testSerializationEmpty() {

    var request = ReceiverLocationsAndTypes.from(Optional.empty(), Optional.empty(), Map.of());
    TestUtilities.assertSerializes(request, ReceiverLocationsAndTypes.class);
  }

  @Test
  void testSerialization() throws IOException {
    var receiverMap = new HashMap<String, Location>();
    receiverMap.put("TestLocation", Location.from(100.0, 50.0, 50.0, 100.0));
    receiverMap.put("TestLocation2", Location.from(100.0, 50.0, 50.0, 100.0));
    var request = ReceiverLocationsAndTypes.from(Optional.of(ChannelDataType.SEISMIC), Optional.of(ChannelBandType.EXTREMELY_LONG_PERIOD), receiverMap);
    TestUtilities.assertSerializes(request, ReceiverLocationsAndTypes.class);
  }
}
