package gms.shared.stationdefinition.coi.utils;

import java.time.Instant;
import java.util.Optional;

public interface StationDefinitionObject {

  public abstract Optional<Instant> getEffectiveAt();
  public abstract Optional<Instant> getEffectiveUntil();
  public abstract Optional<Boolean> getEffectiveAtUpdatedByResponse();
  public abstract Optional<Boolean> getEffectiveUntilUpdatedByResponse();
  public abstract String getName();

  public abstract StationDefinitionObject setEffectiveUntil(Instant effectiveUntil);
  public abstract StationDefinitionObject setEffectiveAt(Instant effectiveAt);
  public abstract StationDefinitionObject setEffectiveAtUpdatedByResponse(Boolean effectiveAtUpdatedByResponse);
  public abstract StationDefinitionObject setEffectiveUntilUpdatedByResponse(Boolean effectiveUntilUpdatedByResponse);

}
