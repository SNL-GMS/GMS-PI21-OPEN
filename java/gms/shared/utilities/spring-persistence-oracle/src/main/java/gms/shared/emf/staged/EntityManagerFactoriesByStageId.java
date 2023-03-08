package gms.shared.emf.staged;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import gms.shared.workflow.coi.WorkflowDefinitionId;

import javax.persistence.EntityManagerFactory;

@AutoValue
public abstract class EntityManagerFactoriesByStageId {

  public abstract ImmutableMap<WorkflowDefinitionId, EntityManagerFactory> getStageIdEmfMap();

  public static Builder builder() {
    return new AutoValue_EntityManagerFactoriesByStageId.Builder();
  }


  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    public abstract Builder setStageIdEmfMap(
      ImmutableMap<WorkflowDefinitionId, EntityManagerFactory> stageIdEmfMap);

    abstract EntityManagerFactoriesByStageId autoBuild();

    public EntityManagerFactoriesByStageId build() {
      return autoBuild();
    }
  }


}
