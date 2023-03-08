package gms.shared.event.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import gms.shared.event.coi.EventStatusInfo;
import gms.shared.workflow.coi.WorkflowDefinitionId;

import java.util.Map;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

/**
 * Defines the response body for the findEventStatusInfoByStageIdAndEventIds operations. Associates a Set of
 * {@link EventStatusInfo} objects with a {@link WorkflowDefinitionId}.
 */
@AutoValue
@JsonSerialize(as = EventStatusInfoByStageIdAndEventIdsResponse.class)
@JsonDeserialize(builder = AutoValue_EventStatusInfoByStageIdAndEventIdsResponse.Builder.class)
public abstract class EventStatusInfoByStageIdAndEventIdsResponse {
  /**
   * Gets the StageId that corresponds to the retrieved {@link EventStatusInfo} objects
   *
   * @return A {@link WorkflowDefinitionId} representing the stageId
   */
  @JsonProperty("stageId")
  public abstract WorkflowDefinitionId getStageId();

  /**
   * Gets the {@link ImmutableMap} linking each event {@link UUID} to its corresponding {@link EventStatusInfo}
   *
   * @return ImmutableMap with K,V of {@link UUID}, {@link EventStatusInfo}
   */
  @JsonUnwrapped
  @JsonProperty(access = READ_ONLY)
  public abstract ImmutableMap<UUID, EventStatusInfo> getEventStatusInfoMap();

  public static Builder builder() {
    return new AutoValue_EventStatusInfoByStageIdAndEventIdsResponse.Builder();
  }

  public abstract Builder toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {
    Builder setStageId(WorkflowDefinitionId stageId);

    default Builder setEventStatusInfoMap(Map<UUID, EventStatusInfo> eventIdToEventStatusInfoMap) {
      setEventStatusInfoMap(ImmutableMap.copyOf(eventIdToEventStatusInfoMap));
      return this;
    }

    Builder setEventStatusInfoMap(ImmutableMap<UUID, EventStatusInfo> eventIdToEventStatusInfoMap);

    ImmutableMap.Builder<UUID, EventStatusInfo> eventStatusInfoMapBuilder();

    default EventStatusInfoByStageIdAndEventIdsResponse.Builder addEventIdToEventStatusInfo(
      UUID eventId, EventStatusInfo eventStatusInfo) {
      eventStatusInfoMapBuilder().put(eventId, eventStatusInfo);
      return this;
    }

    EventStatusInfoByStageIdAndEventIdsResponse autoBuild();

    default EventStatusInfoByStageIdAndEventIdsResponse build() {
      return autoBuild();
    }
  }
}
