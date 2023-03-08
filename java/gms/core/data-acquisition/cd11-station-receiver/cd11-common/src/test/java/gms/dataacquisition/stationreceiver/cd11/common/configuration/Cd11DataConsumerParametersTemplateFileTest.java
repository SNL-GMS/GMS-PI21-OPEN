package gms.dataacquisition.stationreceiver.cd11.common.configuration;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Cd11DataConsumerParametersTemplateFileTest {

  @Test
  void testFrom() {
    Cd11DataConsumerParametersTemplate template1 = Cd11DataConsumerParametersTemplate.builder()
      .setStationName("Name1").setPortOffset(1).setAcquired(true).setFrameProcessingDisabled(true)
      .build();
    Cd11DataConsumerParametersTemplate template2 = Cd11DataConsumerParametersTemplate.builder()
      .setStationName("Name2").setPortOffset(2).setAcquired(true).setFrameProcessingDisabled(true)
      .build();

    ImmutableList<Cd11DataConsumerParametersTemplate> list = ImmutableList.of(template1, template2);

    Cd11DataConsumerParametersTemplatesFile result = Cd11DataConsumerParametersTemplatesFile
      .from(list);

    assertEquals("Name1", result.getCd11DataConsumerParametersTemplates().get(0).getStationName());
    assertEquals("Name2", result.getCd11DataConsumerParametersTemplates().get(1).getStationName());
  }
}
