package gms.shared.utilities.coidataloader;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.dataacquisition.cssreader.waveformreaders.FlatFileWaveformReader;
import gms.shared.frameworks.osd.coi.channel.Channel;
import gms.shared.frameworks.osd.coi.channel.ChannelSegment;
import gms.shared.frameworks.osd.coi.channel.ChannelSegment.Type;
import gms.shared.frameworks.osd.coi.channel.ReferenceChannel;
import gms.shared.frameworks.osd.coi.dataacquisition.SegmentClaimCheck;
import gms.shared.frameworks.osd.coi.event.Event;
import gms.shared.frameworks.osd.coi.signaldetection.QcMask;
import gms.shared.frameworks.osd.coi.signaldetection.Response;
import gms.shared.frameworks.osd.coi.signaldetection.SignalDetection;
import gms.shared.frameworks.osd.coi.signaldetection.StationGroup;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceNetwork;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceNetworkMembership;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceResponse;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceSensor;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceSite;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceSiteMembership;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceStation;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceStationMembership;
import gms.shared.frameworks.osd.coi.waveforms.Waveform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoiLoaderApplicationTests {

  private static final String wfDir = randomString();

  @Mock
  private CoiLoaderArgs args;

  @Mock
  private ObjectMapper objectMapper;

  @Mock
  private CoiLoader loader;

  @Mock
  private FlatFileWaveformReader wfReader;

  private CoiLoaderApplication application;

  private ArgumentCaptor<CoiDataSet> dataCaptor;

  @BeforeEach
  void init() {
    dataCaptor = ArgumentCaptor.forClass(CoiDataSet.class);
    doNothing().when(loader).load(dataCaptor.capture());
    final CoiLoaderApplication app = new CoiLoaderApplication(args, objectMapper, wfReader, loader);
    application = spy(app);
  }

  @Test
  void testExecuteNoArgsDoesNothing() {
    application.execute();
    verifyNoInteractions(objectMapper);
    assertEqual(CoiDataSet.builder().build(), dataCaptor.getValue());
  }

  @Test
  void testExecuteAllArgsCallsCoiLoader() throws IOException {
    final CoiDataSet dataSet = mockDataWithClaimChecks();
    mockArgAsFile(args.getEvents(), dataSet.getEvents(), Event[].class);
    mockArgAsFile(args.getSigDets(), dataSet.getSignalDetections(), SignalDetection[].class);
    mockArgAsFile(args.getMasks(), dataSet.getMasks(), QcMask[].class);
    mockArgAsFile(args.getStationGroups(), dataSet.getStationGroups(), StationGroup[].class);
    mockArgAsFile(args.getProcessingResponses(), dataSet.getResponses(), Response[].class);
    final StationReference staRef = dataSet.getStationReference();
    mockArgAsFile(args.getRefNetworks(), staRef.getNetworks(), ReferenceNetwork[].class);
    mockArgAsFile(args.getRefStations(), staRef.getStations(), ReferenceStation[].class);
    mockArgAsFile(args.getRefSites(), staRef.getSites(), ReferenceSite[].class);
    mockArgAsFile(args.getRefChans(), staRef.getChannels(), ReferenceChannel[].class);
    mockArgAsFile(args.getRefSensors(), staRef.getSensors(), ReferenceSensor[].class);
    mockResponseArg();
    mockFkArg();
    mockArgAsFile(args.getRefNetMemberships(), staRef.getNetworkMemberships(),
      ReferenceNetworkMembership[].class);
    mockArgAsFile(args.getRefStaMemberships(), staRef.getStationMemberships(),
      ReferenceStationMembership[].class);
    mockArgAsFile(args.getRefSiteMemberships(), staRef.getSiteMemberships(),
      ReferenceSiteMembership[].class);

    application.execute();
    assertEqual(dataSet, dataCaptor.getValue());
  }

  // setup mocks for waveform reading; bit involved since it normally stitches them together
  // from segment claim checks and 'w' files
  private CoiDataSet mockDataWithClaimChecks() throws IOException {
    final SegmentClaimCheck claimCheck1 = makeClaimCheck(mock(Channel.class));
    final SegmentClaimCheck claimCheck2 = makeClaimCheck(mock(Channel.class));
    mockArgAsFile(args.getWaveformClaimCheck(), List.of(claimCheck1, claimCheck2),
      SegmentClaimCheck[].class);
    when(args.getWfDir()).thenReturn(wfDir);
    final double[] samples1 = new double[]{1.0, 2.0};
    final double[] samples2 = new double[]{3.0, 4.0};
    mockWfReaderForClaimCheck(Map.of(claimCheck1, samples1, claimCheck2, samples2));
    return MockData.createDataSet().toBuilder()
      // replace waveforms in mock data with ones created specially with claim checks
      .setWaveforms(Stream.of(toSegment(claimCheck1, samples1), toSegment(claimCheck2, samples2)))
      .build();
  }

  private void mockWfReaderForClaimCheck(Map<SegmentClaimCheck, double[]> claimChecksToSamples)
    throws IOException {
    for (Entry<SegmentClaimCheck, double[]> entry : claimChecksToSamples.entrySet()) {
      SegmentClaimCheck cc = entry.getKey();
      double[] value = entry.getValue();
      doReturn(value).when(wfReader).readWaveform(
        wfDir + File.separator + cc.getWaveformFile(),
        cc.getfOff(), cc.getSampleCount(), cc.getDataType());
    }
  }

  private <T> void mockArgAsFile(String argMethodCall,
    Collection<T> coll, Class<T[]> clazz) throws IOException {
    final String path = UUID.randomUUID().toString();
    when(argMethodCall).thenReturn(path);
    doReturn(coll.toArray()).when(objectMapper).readValue(new File(path), clazz);
  }

  private void mockResponseArg() throws IOException {
    final String responsesDir = randomString();
    when(args.getRefResponseDir()).thenReturn(responsesDir);
    final File responseFile1 = new File("foo"); // note: using mocks for these doesn't work
    final File responseFile2 = new File("bar");
    // mock what happens when the directory is listed
    doReturn(new File[]{responseFile1, responseFile2}).when(application).listFiles(responsesDir);
    doReturn(MockData.RESPONSE_1).when(objectMapper).readValue(responseFile1, ReferenceResponse.class);
    doReturn(MockData.RESPONSE_2).when(objectMapper).readValue(responseFile2, ReferenceResponse.class);
  }

  private void mockFkArg() throws IOException {
    final String fkDir = randomString();
    when(args.getFkDir()).thenReturn(fkDir);
    final File f1 = new File("baz"); // note: using mocks for these doesn't work
    final File f2 = new File("boo");
    doReturn(new File[]{f1, f2}).when(application).listFiles(fkDir);
    doReturn(MockData.FK_SEGMENT_1).when(objectMapper).readValue(f1, CoiLoaderApplication.fkSegmentType);
    doReturn(MockData.FK_SEGMENT_2).when(objectMapper).readValue(f2, CoiLoaderApplication.fkSegmentType);
  }

  // can't compare directly because different instances of Stream objects in CoiDataSet are never equal
  private static void assertEqual(CoiDataSet d1, CoiDataSet d2) {
    assertEquals(d1.getStationReference(), d2.getStationReference());
    assertEquals(d1.getStationGroups(), d2.getStationGroups());
    assertEquals(d1.getEvents(), d2.getEvents());
    assertEquals(d1.getSignalDetections(), d2.getSignalDetections());
    assertEquals(d1.getMasks(), d2.getMasks());
    assertEquals(d1.getFks().collect(Collectors.toSet()), d2.getFks().collect(Collectors.toSet()));
    assertEquals(d1.getWaveforms().collect(Collectors.toSet()),
      d2.getWaveforms().collect(Collectors.toSet()));
  }

  private static ChannelSegment<Waveform> toSegment(SegmentClaimCheck cc, double[] samples) {
    return ChannelSegment.from(cc.getSegmentId(), cc.getChannel(),
      cc.getSegmentName(), cc.getSegmentType(),
      List.of(Waveform.from(cc.getStartTime(), cc.getSampleRate(), samples)));
  }

  // note: these values are fairly random, just need some populated
  private static SegmentClaimCheck makeClaimCheck(Channel c) {
    return SegmentClaimCheck.from(UUID.randomUUID(), c,
      randomString(), Instant.EPOCH, 40.0,
      randomString(), 2, 3,
      randomString(), Type.ACQUIRED, false);
  }

  private static String randomString() {
    return UUID.randomUUID().toString();
  }
}
