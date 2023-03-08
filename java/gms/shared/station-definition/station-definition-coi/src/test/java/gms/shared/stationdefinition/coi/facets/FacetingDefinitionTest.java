package gms.shared.stationdefinition.coi.facets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FacetingDefinitionTest {

  private static final Logger logger = LoggerFactory.getLogger(FacetingDefinitionTest.class);

  private ObjectMapper mapper;
  public static String STATION_GROUP_TYPE = "StationGroup";
  public static String STATION_TYPE = "Station";
  public static String STATIONS_KEY = "stations";
  public static String CHANNEL_GROUP_TYPE = "ChannelGroup";
  public static String CHANNEL_GROUPS_KEY = "channelGroups";
  public static String CHANNEL_TYPE = "Channel";
  public static String CHANNELS_KEY = "channels";

  @BeforeEach
  void testSetup() {
    mapper = getJsonObjectMapper();
  }

  @Test
  void testFacetingDefinition_create_noAttributes_serializeToAndFrom()
    throws JsonProcessingException {
    final FacetingDefinition facetingDefinition = getFacetingDefinition("testFacetingDefinition",
      true, new HashMap<>());

    final String json = mapper.writeValueAsString(facetingDefinition);
    logger.info("json serialized faceting definition: {}", json);

    final FacetingDefinition deserialized = mapper.readValue(json, FacetingDefinition.class);
    assertEquals(facetingDefinition, deserialized);
    assertEquals(facetingDefinition, deserialized);
  }

  @Test
  void testFacetingDefinition_create_oneSubFacetingDefinition_serializeToAndFrom()
    throws JsonProcessingException {

    final var childFacetingDefinition = getFacetingDefinition("childFacetingDefinition",
      true, new HashMap<>());
    final FacetingDefinition facetingDefinition = getFacetingDefinition("parentFacetingDefinition",
      true, Map.of("bob", childFacetingDefinition));

    final String json = mapper.writeValueAsString(facetingDefinition);
    logger.info("json serialized faceting definition: {}", json);

    final FacetingDefinition deserialized = mapper.readValue(json, FacetingDefinition.class);
    assertEquals(facetingDefinition, deserialized);
    assertEquals(facetingDefinition.getFacetingDefinitionByName("bob"), childFacetingDefinition);
  }

  @Test
  void testFacetingDefinition_create_emptyFacetingDefinitionMap() {
    final FacetingDefinition facetingDefinition = getEmptyFacetingDefinition("parentFacetingDefinition",
      false);

    assertNull(facetingDefinition.getFacetingDefinitionByName("bob"));
  }

  @Test
  void testChannelGroupUnpopulatedWithFacetingDefinition() {
    assertThrows(IllegalStateException.class, () -> FacetingDefinition.builder()
      .setClassType(CHANNEL_GROUP_TYPE)
      .setPopulated(false)
      .addFacetingDefinitions(CHANNELS_KEY, FacetingDefinition.builder()
        .setClassType(CHANNEL_TYPE)
        .setPopulated(true)
        .build())
      .build());
  }

  @Test
  void testStationUnpopulatedWithFacetingDefinition() {
    assertThrows(IllegalStateException.class, () -> FacetingDefinition.builder()
      .setClassType(STATION_TYPE)
      .setPopulated(false)
      .addFacetingDefinitions(CHANNEL_GROUPS_KEY, FacetingDefinition.builder()
        .setClassType(CHANNEL_GROUP_TYPE)
        .setPopulated(true)
        .build())
      .build());
  }

  @Test
  void testStationGroupUnpopulatedWithFacetingDefinition() {
    assertThrows(IllegalStateException.class, () -> FacetingDefinition.builder()
      .setClassType(STATION_GROUP_TYPE)
      .setPopulated(false)
      .addFacetingDefinitions(STATIONS_KEY, FacetingDefinition.builder()
        .setClassType(STATION_TYPE)
        .setPopulated(true)
        .build())
      .build());
  }

  @Test
  void testFacetingDefinition_create_withNestedFacetingDefinitions_serializeToAndFrom()
    throws JsonProcessingException {

    final var grandchildFacetingDefinition = getFacetingDefinition("grandchildFacetingDefinition",
      true, new HashMap<>());
    final var childFacetingDefinition = getFacetingDefinition("childFacetingDefinition",
      true, Map.of("bill", grandchildFacetingDefinition));
    final FacetingDefinition facetingDefinition = getFacetingDefinition("parentFacetingDefinition",
      true, Map.of("bob", childFacetingDefinition));

    final String json = mapper.writeValueAsString(facetingDefinition);
    logger.info("json serialized faceting definition: {}", json);

    final FacetingDefinition deserialized = mapper.readValue(json, FacetingDefinition.class);
    assertEquals(facetingDefinition, deserialized);
  }

  protected ObjectMapper getJsonObjectMapper() {
    return new ObjectMapper()
      .findAndRegisterModules()
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      .disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
  }

  protected FacetingDefinition getFacetingDefinition(
    String classType, boolean isPopulated, Map<String, FacetingDefinition> facetedDefinitionMap) {
    return FacetingDefinition.builder().setClassType(classType)
      .setPopulated(isPopulated)
      .setFacetingDefinitions(facetedDefinitionMap)
      .build();
  }

  protected FacetingDefinition getEmptyFacetingDefinition(
    String classType, boolean isPopulated) {
    return FacetingDefinition.builder().setClassType(classType)
      .setPopulated(isPopulated)
      .build();
  }
}