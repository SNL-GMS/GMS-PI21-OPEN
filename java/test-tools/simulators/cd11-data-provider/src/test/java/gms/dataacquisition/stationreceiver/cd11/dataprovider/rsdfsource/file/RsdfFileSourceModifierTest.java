package gms.dataacquisition.stationreceiver.cd11.dataprovider.rsdfsource.file;

import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrame;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrameMetadata;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFramePayloadFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

class RsdfFileSourceModifierTest {

  @Test
  void testModifyRawPayload() {
    var rsdfFileSourceModifier = new RsdfFileSourceModifier(Duration.ZERO, 32L);
    var malformedRawData = new byte[]{(byte) 0xC0, 0x00, 0x00, 0x00};
    var rsdf = RawStationDataFrame.builder()
      .setId(UUID.randomUUID())
      .setMetadata(RawStationDataFrameMetadata.builder().setPayloadFormat(RawStationDataFramePayloadFormat.CD11)
        .setStationName("blah")
        .setPayloadEndTime(Instant.EPOCH)
        .setReceptionTime(Instant.EPOCH)
        .setPayloadStartTime(Instant.EPOCH)
        .setChannelNames(Collections.emptyList())
        .setAuthenticationStatus(RawStationDataFrame.AuthenticationStatus.NOT_APPLICABLE)
        .setWaveformSummaries(Map.of()).build()).build();

    Assertions.assertThrows(IllegalStateException.class, () -> rsdfFileSourceModifier.modifyRawPayload(rsdf, Instant.EPOCH));

    final var rsdfWtihMalFormedData = rsdf.toBuilder().setRawPayload(malformedRawData).build();
    Assertions.assertEquals(malformedRawData, rsdfFileSourceModifier.modifyRawPayload(rsdfWtihMalFormedData, Instant.EPOCH));
  }
}
