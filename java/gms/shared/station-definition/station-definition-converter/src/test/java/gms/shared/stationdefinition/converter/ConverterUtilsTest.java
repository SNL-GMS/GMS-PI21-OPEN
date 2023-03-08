package gms.shared.stationdefinition.converter;

import gms.shared.stationdefinition.coi.channel.Channel;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.OFFDATE;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.ONDATE;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.ONDATE2;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.ONDATE3;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.ONDATE4;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.CHANNEL_STA01_STA01_BHE;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConverterUtilsTest {

  @Test
  void getUpdatedByResponse_channelUpdatedIsFalse() {

    List<Channel> channels = List.of(
      CHANNEL_STA01_STA01_BHE.toBuilder().setEffectiveAt(ONDATE)
        .setData(CHANNEL_STA01_STA01_BHE.getData().orElseThrow().toBuilder().setEffectiveUntil(OFFDATE)
          .build())
        .build());
    Pair<Boolean, Boolean> pair = ConverterUtils.getUpdatedByResponse(channels, ONDATE, OFFDATE);
    System.out.println(pair);

    assertFalse(pair.getLeft().booleanValue());
    assertFalse(pair.getRight().booleanValue());
  }

  @Test
  void getUpdatedByResponse_channelUpdatedIsTrue() {
    List<Channel> channels = List.of(
      CHANNEL_STA01_STA01_BHE.toBuilder().setEffectiveAt(ONDATE)
        .setData(CHANNEL_STA01_STA01_BHE.getData().orElseThrow().toBuilder()
          .setEffectiveUntil(OFFDATE)
          .setEffectiveAtUpdatedByResponse(Optional.of(true))
          .setEffectiveUntilUpdatedByResponse(Optional.of(true))
          .build())
        .build());
    Pair<Boolean, Boolean> pair = ConverterUtils.getUpdatedByResponse(channels, ONDATE, OFFDATE);
    System.out.println(pair);

    assertTrue(pair.getLeft().booleanValue());
    assertTrue(pair.getRight().booleanValue());
  }

  @Test
  void getUpdatedByResponse_channelUpdatedIsTrue_TimesDoNotMatch() {
    List<Channel> channels = List.of(
      CHANNEL_STA01_STA01_BHE.toBuilder().setEffectiveAt(ONDATE)
        .setData(CHANNEL_STA01_STA01_BHE.getData().orElseThrow().toBuilder()
          .setEffectiveUntil(ONDATE2)
          .setEffectiveAtUpdatedByResponse(Optional.of(true))
          .setEffectiveUntilUpdatedByResponse(Optional.of(true))
          .build())
        .build());
    Pair<Boolean, Boolean> pair = ConverterUtils.getUpdatedByResponse(channels, ONDATE3, ONDATE4);
    System.out.println(pair);

    assertFalse(pair.getLeft().booleanValue());
    assertFalse(pair.getRight().booleanValue());
  }
}
