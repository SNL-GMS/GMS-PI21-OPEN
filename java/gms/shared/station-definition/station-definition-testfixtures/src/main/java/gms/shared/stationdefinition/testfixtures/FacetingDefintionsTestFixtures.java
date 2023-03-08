package gms.shared.stationdefinition.testfixtures;

import gms.shared.stationdefinition.coi.facets.FacetingDefinition;

import static gms.shared.stationdefinition.facet.FacetingTypes.CHANNELS_KEY;
import static gms.shared.stationdefinition.facet.FacetingTypes.CHANNEL_GROUPS_KEY;
import static gms.shared.stationdefinition.facet.FacetingTypes.CHANNEL_GROUP_TYPE;
import static gms.shared.stationdefinition.facet.FacetingTypes.CHANNEL_TYPE;
import static gms.shared.stationdefinition.facet.FacetingTypes.STATIONS_KEY;
import static gms.shared.stationdefinition.facet.FacetingTypes.STATION_GROUP_TYPE;
import static gms.shared.stationdefinition.facet.FacetingTypes.STATION_TYPE;

public class FacetingDefintionsTestFixtures {

  /**
   * Private constructor to hide the implicit public one.
   */
  private FacetingDefintionsTestFixtures() {
  }

  public static final FacetingDefinition STATIONGROUP_POPULATED_FULL = FacetingDefinition.builder()
    .setClassType(STATION_GROUP_TYPE.getValue())
    .setPopulated(true)
    .addFacetingDefinitions(STATIONS_KEY.getValue(), FacetingDefinition.builder()
      .setClassType(STATION_TYPE.getValue())
      .setPopulated(true)
      .addFacetingDefinitions(CHANNEL_GROUPS_KEY.getValue(), FacetingDefinition.builder()
        .setClassType(CHANNEL_GROUP_TYPE.getValue())
        .setPopulated(true)
        .addFacetingDefinitions(CHANNELS_KEY.getValue(), FacetingDefinition.builder()
          .setClassType(CHANNEL_TYPE.getValue())
          .setPopulated(true)
          .build())
        .build())
      .addFacetingDefinitions(CHANNELS_KEY.getValue(), FacetingDefinition.builder()
        .setClassType(CHANNEL_TYPE.getValue())
        .setPopulated(true)
        .build())
      .build())
    .build();

  public static final FacetingDefinition STATIONGROUP_CHANNELS_POPULATED = FacetingDefinition.builder()
    .setClassType(STATION_GROUP_TYPE.getValue())
    .setPopulated(true)
    .addFacetingDefinitions(STATIONS_KEY.getValue(), FacetingDefinition.builder()
      .setClassType(STATION_TYPE.getValue())
      .setPopulated(true)
      .addFacetingDefinitions(CHANNELS_KEY.getValue(), FacetingDefinition.builder()
        .setClassType(CHANNEL_TYPE.getValue())
        .setPopulated(true)
        .build())
      .build())
    .build();

  public static final FacetingDefinition STATIONGROUP_POPULATED_PARTIAL = FacetingDefinition.builder()
    .setClassType(STATION_GROUP_TYPE.getValue())
    .setPopulated(true)
    .addFacetingDefinitions(STATIONS_KEY.getValue(), FacetingDefinition.builder()
      .setClassType(STATION_TYPE.getValue())
      .setPopulated(true)
      .build())
    .build();

  public static final FacetingDefinition STATIONGROUP_POPULATED_EMPTYFACETS = FacetingDefinition.builder()
    .setClassType(STATION_GROUP_TYPE.getValue())
    .setPopulated(true)
    .build();

  public static final FacetingDefinition STATIONGROUP_NOTPOPULATED_EMPTYFACETS = FacetingDefinition.builder()
    .setClassType(STATION_GROUP_TYPE.getValue())
    .setPopulated(false)
    .build();

  public static final FacetingDefinition STATION_POPULATED_FULL =
    FacetingDefinition.builder()
      .setClassType(STATION_TYPE.getValue())
      .setPopulated(true)
      .addFacetingDefinitions(CHANNEL_GROUPS_KEY.getValue(), FacetingDefinition.builder()
        .setClassType(CHANNEL_GROUP_TYPE.getValue())
        .setPopulated(true)
        .addFacetingDefinitions(CHANNELS_KEY.getValue(), FacetingDefinition.builder()
          .setClassType(CHANNEL_TYPE.getValue())
          .setPopulated(true)
          .build())
        .build())
      .addFacetingDefinitions(CHANNELS_KEY.getValue(), FacetingDefinition.builder()
        .setClassType(CHANNEL_TYPE.getValue())
        .setPopulated(true)
        .build())
      .build();

  public static final FacetingDefinition STATION_POPULATED_PARTIAL =
    FacetingDefinition.builder()
      .setClassType(STATION_TYPE.getValue())
      .setPopulated(true)
      .addFacetingDefinitions(CHANNEL_GROUPS_KEY.getValue(), FacetingDefinition.builder()
        .setClassType(CHANNEL_GROUP_TYPE.getValue())
        .setPopulated(true)
        .build())
      .build();

  public static final FacetingDefinition STATION_POPULATED_EMPTYFACETS = FacetingDefinition.builder()
    .setClassType(STATION_TYPE.getValue())
    .setPopulated(true)
    .build();

  public static final FacetingDefinition STATION_NOTPOPULATED_EMPTYFACETS = FacetingDefinition.builder()
    .setClassType(STATION_TYPE.getValue())
    .setPopulated(false)
    .build();


  public static final FacetingDefinition CHANNELGROUP_POPULATED_FULL = FacetingDefinition.builder()
    .setClassType(CHANNEL_GROUP_TYPE.getValue())
    .setPopulated(true)
    .addFacetingDefinitions(CHANNELS_KEY.getValue(), FacetingDefinition.builder()
      .setClassType(CHANNEL_TYPE.getValue())
      .setPopulated(true)
      .build())
    .build();

  public static final FacetingDefinition CHANNELGROUP_POPULATED_EMPTYFACETS = FacetingDefinition.builder()
    .setClassType(CHANNEL_GROUP_TYPE.getValue())
    .setPopulated(true)
    .build();

  public static final FacetingDefinition CHANNELGROUP_NOTPOPULATED_EMPTYFACETS = FacetingDefinition.builder()
    .setClassType(CHANNEL_GROUP_TYPE.getValue())
    .setPopulated(false)
    .build();

  public static final FacetingDefinition CHANNEL_POPULATED_EMPTYFACETS = FacetingDefinition.builder()
    .setClassType(CHANNEL_TYPE.getValue())
    .setPopulated(true)
    .build();

  public static final FacetingDefinition CHANNEL_NOTPOPULATED_EMPTYFACETS = FacetingDefinition.builder()
    .setClassType(CHANNEL_TYPE.getValue())
    .setPopulated(false)
    .build();

}
