package gms.dataacquisition.stationreceiver.cd11.common.configuration;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class Cd11DataConsumerParametersTest {


  @Mock
  Cd11DataConsumerParametersTemplate template;

  @Test
  void testCreate() {
    given(template.getPortOffset()).willReturn(1);
    given(template.getStationName()).willReturn("StationName");
    given(template.isAcquired()).willReturn(false);
    given(template.isFrameProcessingDisabled()).willReturn(true);

    Cd11DataConsumerParameters params = Cd11DataConsumerParameters.create(template, 8050);

    assertEquals(8051, params.getPort());
    assertEquals("StationName", params.getStationName());
    assertTrue(params.isFrameProcessingDisabled());
    assertFalse(params.isAcquired());
  }
}
