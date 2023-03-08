package gms.shared.frameworks.osd.api.rawstationdataframe;

import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue.AcquiredChannelEnvironmentIssueType;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueAnalog;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueBoolean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.time.Instant.EPOCH;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AceiUpdatesTests {


  @Test
  void testBuild() {
    AcquiredChannelEnvironmentIssueAnalog aAnalogAdd = buildAceiAnalog(0, 5);
    AcquiredChannelEnvironmentIssueAnalog aAnalogDelete = buildAceiAnalog(5, 10);

    AcquiredChannelEnvironmentIssueBoolean aBooleanAdd = buildAceiBoolean(0, 5);
    AcquiredChannelEnvironmentIssueBoolean aBooleanDelete = buildAceiBoolean(5, 10);

    AceiUpdates actualUpdateSets = AceiUpdates.builder()
      .setBooleanInserts(singleton(aBooleanAdd))
      .setBooleanDeletes(singleton(aBooleanDelete))
      .setAnalogInserts(singleton(aAnalogAdd))
      .setAnalogDeletes(singleton(aAnalogDelete))
      .build();

    assertAll(
      () -> assertEquals(1, actualUpdateSets.getAnalogInserts().size()),
      () -> assertEquals(1, actualUpdateSets.getAnalogDeletes().size()),
      () -> assertEquals(1, actualUpdateSets.getBooleanInserts().size()),
      () -> assertEquals(1, actualUpdateSets.getBooleanDeletes().size()));

    assertTrue(actualUpdateSets.getAnalogInserts().contains(aAnalogAdd));
    assertTrue(actualUpdateSets.getAnalogDeletes().contains(aAnalogDelete));
    assertTrue(actualUpdateSets.getBooleanInserts().contains(aBooleanAdd));
    assertTrue(actualUpdateSets.getBooleanDeletes().contains(aBooleanDelete));
  }

  @Test
  void testEmptyBuild() {
    AceiUpdates actualUpdateSets = AceiUpdates.builder().build();

    assertTrue(actualUpdateSets.isEmpty());
  }

  @ParameterizedTest
  @MethodSource("updatesFromAceiSource")
  void testFromAcei(AcquiredChannelEnvironmentIssue<?> input, AceiUpdates expected) {
    assertEquals(expected, AceiUpdates.from(input));
  }

  public static Stream<Arguments> updatesFromAceiSource() {
    AcquiredChannelEnvironmentIssueAnalog aceiAnalog = buildAceiAnalog(0, 1);
    AcquiredChannelEnvironmentIssueBoolean aceiBoolean = buildAceiBoolean(0, 1);
    return Stream.of(
      Arguments.arguments(
        aceiAnalog, AceiUpdates.builder().addAnalogInsert(aceiAnalog).build()
      ),
      Arguments.arguments(
        aceiBoolean, AceiUpdates.builder().addBooleanInsert(aceiBoolean).build()
      )
    );
  }

  @ParameterizedTest
  @MethodSource("updatesFromAceiCollectionSource")
  void testFromAceiCollection(Collection<AcquiredChannelEnvironmentIssue<?>> input, AceiUpdates expected) {
    assertEquals(expected, AceiUpdates.from(input));
  }

  public static Stream<Arguments> updatesFromAceiCollectionSource() {
    AcquiredChannelEnvironmentIssueAnalog aceiAnalog = buildAceiAnalog(0, 1);
    AcquiredChannelEnvironmentIssueBoolean aceiBoolean = buildAceiBoolean(0, 1);
    return Stream.of(
      Arguments.arguments(
        List.of(aceiAnalog), AceiUpdates.builder().addAnalogInsert(aceiAnalog).build()
      ),
      Arguments.arguments(
        List.of(aceiBoolean), AceiUpdates.builder().addBooleanInsert(aceiBoolean).build()
      ),
      Arguments.arguments(
        List.of(aceiAnalog, aceiBoolean), AceiUpdates.builder().addAnalogInsert(aceiAnalog).addBooleanInsert(aceiBoolean).build()
      )
    );
  }

  @Test
  void testBuildDuplicateRemoval() {

    AcquiredChannelEnvironmentIssueAnalog aAnalogAdd = buildAceiAnalog(0, 5);
    AcquiredChannelEnvironmentIssueAnalog aAnalogDelete = buildAceiAnalog(5, 10);
    AcquiredChannelEnvironmentIssueBoolean aBooleanAdd = buildAceiBoolean(0, 5);
    AcquiredChannelEnvironmentIssueBoolean aBooleanDelete = buildAceiBoolean(5, 10);

    AceiUpdates actualUpdateSets = AceiUpdates.builder()
      .setBooleanInserts(List.of(aBooleanAdd, aBooleanAdd))
      .setBooleanDeletes(List.of(aBooleanDelete, aBooleanDelete))
      .setAnalogInserts(List.of(aAnalogAdd, aAnalogAdd))
      .setAnalogDeletes(List.of(aAnalogDelete, aAnalogDelete))
      .build();

    assertAll(
      () -> assertEquals(singleton(aAnalogAdd), actualUpdateSets.getAnalogInserts()),
      () -> assertEquals(singleton(aAnalogDelete), actualUpdateSets.getAnalogDeletes()),
      () -> assertEquals(singleton(aBooleanAdd), actualUpdateSets.getBooleanInserts()),
      () -> assertEquals(singleton(aBooleanDelete), actualUpdateSets.getBooleanDeletes()));
  }

  @Test
  void testBuildAcceptAddDeleteCollisionsWithoutRemoval() {
    AcquiredChannelEnvironmentIssueBoolean aBooleanIgnore = buildAceiBoolean(0, 5);
    AcquiredChannelEnvironmentIssueBoolean aBooleanAdd = buildAceiBoolean(0, 10);
    AcquiredChannelEnvironmentIssueBoolean aBooleanDelete = buildAceiBoolean(5, 10);

    AcquiredChannelEnvironmentIssueAnalog aAnalogIgnore = buildAceiAnalog(0, 5);
    AcquiredChannelEnvironmentIssueAnalog aAnalogAdd = buildAceiAnalog(0, 10);
    AcquiredChannelEnvironmentIssueAnalog aAnalogDelete = buildAceiAnalog(5, 10);

    var booleanInserts = Set.of(aBooleanIgnore, aBooleanAdd);
    var booleanDeletes = Set.of(aBooleanIgnore, aBooleanDelete);
    var analogInserts = Set.of(aAnalogIgnore, aAnalogAdd);
    var analogDeletes = Set.of(aAnalogIgnore, aAnalogDelete);

    AceiUpdates actualUpdateSets = AceiUpdates.builder()
      .setBooleanInserts(booleanInserts)
      .setBooleanDeletes(booleanDeletes)
      .setAnalogInserts(analogInserts)
      .setAnalogDeletes(analogDeletes)
      .build();

    assertAll(
      () -> assertEquals(analogInserts, actualUpdateSets.getAnalogInserts()),
      () -> assertEquals(analogDeletes, actualUpdateSets.getAnalogDeletes()),
      () -> assertEquals(booleanInserts, actualUpdateSets.getBooleanInserts()),
      () -> assertEquals(booleanDeletes, actualUpdateSets.getBooleanDeletes()));
  }

  private static AcquiredChannelEnvironmentIssueAnalog buildAceiAnalog(long startFromEpoch,
    long endFromEpoch) {
    return AcquiredChannelEnvironmentIssueAnalog
      .from("channel1", AcquiredChannelEnvironmentIssueType.DURATION_OUTAGE,
        EPOCH.plusSeconds(startFromEpoch), EPOCH.plusSeconds(endFromEpoch), 1000);
  }

  private static AcquiredChannelEnvironmentIssueBoolean buildAceiBoolean(long startFromEpoch,
    long endFromEpoch) {
    return AcquiredChannelEnvironmentIssueBoolean
      .from("channel1", AcquiredChannelEnvironmentIssueType.CLIPPED,
        EPOCH.plusSeconds(startFromEpoch),
        EPOCH.plusSeconds(endFromEpoch), true);
  }

  @ParameterizedTest
  @MethodSource("unionOperatorTestSource")
  void testUnionOperator(AceiUpdates left, AceiUpdates right, AceiUpdates expectedUnion) {
    var result = AceiUpdates.UPDATES_UNION_OPERATOR.apply(left, right);

    assertEquals(expectedUnion, result);
  }

  public static Stream<Arguments> unionOperatorTestSource() {

    AcquiredChannelEnvironmentIssueAnalog aAnalog05 = buildAceiAnalog(0, 5);
    AcquiredChannelEnvironmentIssueAnalog aAnalog510 = buildAceiAnalog(5, 10);
    AcquiredChannelEnvironmentIssueBoolean aBoolean05 = buildAceiBoolean(0, 5);
    AcquiredChannelEnvironmentIssueBoolean aBoolean510 = buildAceiBoolean(5, 10);
    AcquiredChannelEnvironmentIssueBoolean aBoolean1015 = buildAceiBoolean(10, 15);

    AceiUpdates allPopulated = AceiUpdates.builder().addAnalogInsert(aAnalog05)
      .addAnalogDelete(aAnalog510)
      .addBooleanInsert(aBoolean05)
      .addBooleanDelete(aBoolean510)
      .build();
    AceiUpdates booleanInsert = AceiUpdates.builder().addBooleanInsert(aBoolean1015).build();


    return Stream.of(
      Arguments.arguments(
        AceiUpdates.emptyUpdates(),
        AceiUpdates.emptyUpdates(),
        AceiUpdates.emptyUpdates()
      ),
      Arguments.arguments(
        allPopulated,
        AceiUpdates.emptyUpdates(),
        allPopulated
      ),
      Arguments.arguments(
        allPopulated,
        allPopulated,
        allPopulated
      ),
      Arguments.arguments(
        allPopulated,
        booleanInsert,
        allPopulated.toBuilder()
          .addBooleanInsert(aBoolean1015)
          .build()
      )
    );
  }
}