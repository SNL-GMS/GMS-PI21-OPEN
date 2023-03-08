package gms.dataacquisition.stationreceiver.cd11.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.core.dataacquisition.receiver.DataFrameReceiverConfiguration;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue.AcquiredChannelEnvironmentIssueType;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueAnalog;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueBoolean;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.frameworks.osd.coi.soh.AcquiredStationSohExtract;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.BDDMockito.willReturn;

@ExtendWith(MockitoExtension.class)
class Cd11AcquiredStationGroupSohStatusExtractParserTest {

  private static final String LBTB1_CHANNEL_NAME = "LBTB.LBTB1.SHZ";
  private static final Instant EXPECTED_START_TIME = Instant.parse("2019-06-06T17:26:00Z");
  private static final Instant EXPECTED_TIME_SERIES_END_TIME = Instant
    .parse("2019-06-06T17:26:09.975Z");

  private Cd11StationSohExtractParser stationSohExtractParser;

  @Mock
  private DataFrameReceiverConfiguration mockConfig;

  @BeforeEach
  void setup() {
    stationSohExtractParser = Cd11StationSohExtractParser
      .create(mockConfig);
  }

  @Test
  void testCreateSohExtractFromRawStationDataFrame() throws IOException {
    initConfigMock();
    //Create RawStationDataFrame from file
    String contents = new String(Files.readAllBytes(Path.of("src/test/resources/gms/data/rsdfs/LBTB.json")));
    final ObjectMapper objMapper = CoiObjectMapperFactory.getJsonObjectMapper();
    RawStationDataFrame rsdf = objMapper.readValue(contents, RawStationDataFrame.class);

    AcquiredStationSohExtract input = stationSohExtractParser.parseStationSohExtract(rsdf);

    //Check top-level fields
    assertEquals(1, input.getAcquisitionMetadata().size());
    assertEquals(rsdf.getMetadata().getWaveformSummaries(),
      input.getAcquisitionMetadata().get(0).getWaveformSummaries());
    assertEquals(rsdf.getMetadata().getReceptionTime(), input.getAcquisitionMetadata().get(0).getReceptionTime());
    assertEquals(rsdf.getMetadata().getPayloadStartTime(), input.getAcquisitionMetadata().get(0).getPayloadStartTime());
    assertEquals(rsdf.getMetadata().getPayloadEndTime(), input.getAcquisitionMetadata().get(0).getPayloadEndTime());

    //Test SOH
    List<AcquiredChannelEnvironmentIssue<?>> statesOfHealth = input.getAcquiredChannelEnvironmentIssues();
    assertEquals(68, statesOfHealth.size());
    List<AcquiredChannelEnvironmentIssue<?>> lbtbSOHs = statesOfHealth.stream()
      .filter(x -> x.getChannelName().equals(LBTB1_CHANNEL_NAME))
      .collect(Collectors.toList());
    assertEquals(17, lbtbSOHs.size());
    //Filter for specific SOH (Boolean)
    AcquiredChannelEnvironmentIssue<?> vaultDoorSOH = lbtbSOHs.stream()
      .filter(x -> x.getType().equals(AcquiredChannelEnvironmentIssueType.VAULT_DOOR_OPENED))
      .findFirst()
      .orElse(null);
    if (vaultDoorSOH != null) {
      AcquiredChannelEnvironmentIssueBoolean vaultDoorSOHBool = (AcquiredChannelEnvironmentIssueBoolean) vaultDoorSOH;
      assertEquals(EXPECTED_START_TIME, vaultDoorSOHBool.getStartTime());
      assertEquals(EXPECTED_TIME_SERIES_END_TIME, vaultDoorSOHBool.getEndTime());
      assertFalse(vaultDoorSOHBool.getStatus());
    } else {
      fail("vaultDoorSOH is null, unable to verify start time, end time, status");
    }

    //Filter for specific SOH (Analog)
    AcquiredChannelEnvironmentIssue<?> clockDiffSOH = lbtbSOHs.stream()
      .filter(x -> x.getType()
      .equals(AcquiredChannelEnvironmentIssueType.CLOCK_DIFFERENTIAL_IN_MICROSECONDS))
      .findFirst()
      .orElse(null);
    if (clockDiffSOH != null) {
      AcquiredChannelEnvironmentIssueAnalog clockDiffSOHAnalog = (AcquiredChannelEnvironmentIssueAnalog) clockDiffSOH;
      assertEquals(EXPECTED_START_TIME, clockDiffSOHAnalog.getStartTime());
      assertEquals(EXPECTED_TIME_SERIES_END_TIME, clockDiffSOHAnalog.getEndTime());
      assertEquals(0.0, clockDiffSOHAnalog.getStatus().doubleValue());
    } else {
      fail(
        "clockDiffSOH is null, unable to verify start time, end time, "
        + "CLOCK_DIFFERENTIAL_IN_MICROSECONDS");
    }
  }

  /**
   * Sets up the mock config return values for both parser methods that use it
   */
  private void initConfigMock() {
    //setup mock config behavior
    willReturn(Optional.of(LBTB1_CHANNEL_NAME))
      .given(mockConfig).getChannelName(LBTB1_CHANNEL_NAME);
    willReturn(Optional.of("LBTB.LBTBB.BHZ"))
      .given(mockConfig).getChannelName("LBTB.LBTBB.BHZ");
    willReturn(Optional.of("LBTB.LBTBB.BHN"))
      .given(mockConfig).getChannelName("LBTB.LBTBB.BHN");
    willReturn(Optional.of("LBTB.LBTBB.BHE"))
      .given(mockConfig).getChannelName("LBTB.LBTBB.BHE");
  }
}
