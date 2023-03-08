package gms.shared.event.repository.util.id;

import com.google.auto.value.AutoValue;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents the necessary data to uniquely identify an {@link gms.shared.event.dao.OriginDao}
 */
@AutoValue
public abstract class OriginUniqueIdentifier {

  /**
   * Gets the orid of the {@link gms.shared.event.dao.OriginDao} this OriginUniqueIdentifier represents
   */
  public abstract long getOrid();

  /**
   * Gets the stage of the {@link gms.shared.event.dao.OriginDao} this OriginUniqueIdentifier represents
   */
  public abstract String getStage();

  /**
   * Creates a new OriginUniqueIdentifier containing the provided orid and stage
   *
   * @param orid The orid of the {@link gms.shared.event.dao.OriginDao} the created OriginUniqueIdentifier represents
   * @param stage The stage of the {@link gms.shared.event.dao.OriginDao} the created OriginUniqueIdentifier represents
   * @return A new OriginUniqueIdentifier
   */
  public static OriginUniqueIdentifier create(long orid, String stage) {

    checkArgument(orid > 0, "orid must be greater than 0");
    checkNotNull(stage, "stage must not be null");
    checkArgument(!stage.isBlank(), "stage must not be blank");
    return new AutoValue_OriginUniqueIdentifier(orid, stage);
  }

}
