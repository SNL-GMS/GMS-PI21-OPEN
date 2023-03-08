package gms.shared.waveform.manager;

import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.spring.utilities.framework.SpringTestBase;
import gms.shared.waveform.api.WaveformAccessorInterface;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import static gms.shared.waveform.testfixture.WaveformRequestTestFixtures.channelSegmentDescriptorRequest;
import static gms.shared.waveform.testfixture.WaveformRequestTestFixtures.channelTimeRangeRequest;
import static gms.shared.waveform.testfixture.WaveformRequestTestFixtures.facetedChannelSegmentDescriptorRequest;
import static gms.shared.waveform.testfixture.WaveformRequestTestFixtures.facetedChannelTimeRangeRequest;
import static org.mockito.Mockito.times;

@WebMvcTest(WaveformManager.class)
class WaveformManagerTest extends SpringTestBase {

  @MockBean
  private SystemConfig systemConfig;

  @MockBean
  private WaveformAccessorInterface waveformAccessorImpl;


  @Test
  void findWaveformsByChannelsAndTimeRange_withoutFacet() throws Exception {

    MockHttpServletResponse response = postResult(
      "/waveform/channel-segment/query/channel-timerange",
      channelTimeRangeRequest,
      HttpStatus.OK);

    Mockito.verify(waveformAccessorImpl, times(1)).findByChannelsAndTimeRange(
      channelTimeRangeRequest.getChannels(),
      channelTimeRangeRequest.getStartTime(),
      channelTimeRangeRequest.getEndTime());
  }

  @Test
  void findWaveformsByChannelsAndTimeRange_withFacet() throws Exception {

    MockHttpServletResponse response = postResult(
      "/waveform/channel-segment/query/channel-timerange",
      facetedChannelTimeRangeRequest,
      HttpStatus.OK);

    Mockito.verify(waveformAccessorImpl, times(1)).findByChannelsAndTimeRange(
      facetedChannelTimeRangeRequest.getChannels(),
      facetedChannelTimeRangeRequest.getStartTime(),
      facetedChannelTimeRangeRequest.getEndTime(),
      facetedChannelTimeRangeRequest.getFacetingDefinition().get());
  }


  @Test
  void findWaveformsByChannelSegmentDescriptors_withoutFacet() throws Exception {
    MockHttpServletResponse response = postResult(
      "/waveform/channel-segment/query/channel-segment-descriptors",
      channelSegmentDescriptorRequest,
      HttpStatus.OK);

    Mockito.verify(waveformAccessorImpl, times(1)).findByChannelSegmentDescriptors(
      channelSegmentDescriptorRequest.getChannelSegmentDescriptors());
  }

  @Test
  void findWaveformsByChannelSegmentDescriptors_withFacet() throws Exception {
    MockHttpServletResponse response = postResult(
      "/waveform/channel-segment/query/channel-segment-descriptors",
      facetedChannelSegmentDescriptorRequest,
      HttpStatus.OK);

    Mockito.verify(waveformAccessorImpl, times(1)).findByChannelNamesAndSegmentDescriptor(
      facetedChannelSegmentDescriptorRequest.getChannelSegmentDescriptors(),
      facetedChannelSegmentDescriptorRequest.getFacetingDefinition().get());
  }
}