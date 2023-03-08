package gms.shared.event.dao;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Aggregates an arrivalId and an originId into a single object
 */
@Embeddable
public class OriginIdArrivalIdKey implements Serializable {

  private long originId;
  private long arrivalId;

  public OriginIdArrivalIdKey() {
  }

  private OriginIdArrivalIdKey(Builder builder) {
    this.originId = builder.originId;
    this.arrivalId = builder.arrivalId;
  }

  @Column(name = "orid")
  public long getOriginId() {
    return originId;
  }

  public void setOriginId(long originId) {
    this.originId = originId;
  }

  @Column(name = "arid")
  public long getArrivalId() {
    return arrivalId;
  }

  public void setArrivalId(long arrivalId) {
    this.arrivalId = arrivalId;
  }

  public static class Builder {

    private long originId;
    private long arrivalId;

    public Builder withOriginId(long originId) {
      this.originId = originId;
      return this;
    }

    public Builder withArrivalId(long arrivalId) {
      this.arrivalId = arrivalId;
      return this;
    }

    public OriginIdArrivalIdKey build() {

      // NA not allowed
      checkArgument(0 < originId, "The value of Origin Id is " + originId +
        DaoHelperUtility.createGreaterThanString(0));

      // NA not allowed
      checkArgument(0 < arrivalId, "The value of Arrival Id is " + arrivalId +
        DaoHelperUtility.createGreaterThanString(0));

      return new OriginIdArrivalIdKey(this);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof OriginIdArrivalIdKey)) {
      return false;
    }
    OriginIdArrivalIdKey that = (OriginIdArrivalIdKey) o;
    return originId == that.originId && arrivalId == that.arrivalId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(originId, arrivalId);
  }

  @Override
  public String toString() {
    return "OriginIdArrivalIdKey{" +
      "originId=" + originId +
      ", arrivalId=" + arrivalId +
      '}';
  }
}
