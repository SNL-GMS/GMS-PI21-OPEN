package gms.shared.signaldetection.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.signaldetection.api.SignalDetectionAccessorInterface;
import gms.shared.signaldetection.api.request.DetectionsWithSegmentsByIdsRequest;
import gms.shared.signaldetection.api.request.DetectionsWithSegmentsByStationsAndTimeRequest;
import gms.shared.spring.utilities.framework.SpringTestBase;
import gms.shared.stationdefinition.coi.station.Station;
import gms.shared.utilities.javautilities.objectmapper.ObjectMapperFactory;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.times;


@EnableWebMvc
@WebMvcTest(SignalDetectionManager.class)
class SignalDetectionManagerTest extends SpringTestBase {

  @MockBean
  private SystemConfig systemConfig;

  @MockBean(name = "bridgedSignalDetectionAccessor")
  private SignalDetectionAccessorInterface signalDetectionAccessorImpl;

  @Autowired
  SignalDetectionManager signalDetectionManager;

  protected ObjectMapper mapper = ObjectMapperFactory.getJsonObjectMapper();

  public MappingJackson2HttpMessageConverter jackson2HttpMessageConverter() {
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
    converter.setObjectMapper(mapper);
    return converter;
  }

  MockMvc altMockMvc;

  @BeforeEach
  void init() {

    altMockMvc = MockMvcBuilders.standaloneSetup(signalDetectionManager)
      .setMessageConverters(jackson2HttpMessageConverter()).build();
  }

  @Test
  void testFindDetectionsWithSegmentsByStationsAndTime() throws Exception {

    Station station = Station.builder().setName("tst").setEffectiveAt(Instant.now()).build();

    var request = DetectionsWithSegmentsByStationsAndTimeRequest.create(
      ImmutableList.of(station), Instant.MIN, Instant.MAX, WorkflowDefinitionId.from("test"), ImmutableList.of());

    postResult("/signal-detection/signal-detections-with-channel-segments/query/stations-timerange",
      request, HttpStatus.OK, altMockMvc);

    Mockito.verify(signalDetectionAccessorImpl, times(1)).findWithSegmentsByStationsAndTime(request.getStations(),
      request.getStartTime(),
      request.getEndTime(),
      request.getStageId(),
      request.getExcludedSignalDetections());
  }

  @Test
  void testFindDetectionsWithSegmentsByIds() throws Exception {

    var request =
      DetectionsWithSegmentsByIdsRequest.create(ImmutableList.of(UUID.randomUUID()), WorkflowDefinitionId.from("test"));

    postResult("/signal-detection/signal-detections-with-channel-segments/query/ids", request, HttpStatus.OK, altMockMvc);

    Mockito.verify(signalDetectionAccessorImpl, times(1)).findWithSegmentsByIds(request.getDetectionIds(), request.getStageId());
  }


}
