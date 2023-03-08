package gms.shared.signaldetection.api.response;

import com.google.common.collect.ImmutableSet;
import gms.shared.signaldetection.coi.detection.FeatureMeasurement;
import gms.shared.signaldetection.coi.detection.SignalDetectionHypothesis;
import gms.shared.utilities.test.TestUtilities;
import gms.shared.waveform.coi.ChannelSegment;
import gms.shared.waveform.coi.Timeseries;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.SIGNAL_DETECTION;

class SignalDetectionsWithChannelSegmentsTest {


  @Test
  void testSerialization() throws IOException {
    List<ChannelSegment<? extends Timeseries>> channelSegments = SIGNAL_DETECTION.getSignalDetectionHypotheses()
      .stream()
      .map(SignalDetectionHypothesis::getFeatureMeasurements)
      .flatMap(Collection::stream)
      .map(FeatureMeasurement::getMeasuredChannelSegment)
      .collect(Collectors.toList());
    SignalDetectionsWithChannelSegments sigDetWithSegments = SignalDetectionsWithChannelSegments.builder()
      .addSignalDetection(SIGNAL_DETECTION)
      .setChannelSegments(ImmutableSet.copyOf(channelSegments))
      .build();

    TestUtilities.assertSerializes(sigDetWithSegments, SignalDetectionsWithChannelSegments.class);
  }

}