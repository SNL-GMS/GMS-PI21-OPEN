package gms.shared.signaldetection.dao.css;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class AridOridKey implements Serializable {

  private long arrivalId;
  private long originId;

  public AridOridKey() {
    // no op jpa constructor
  }

  private AridOridKey(Builder builder) {
    this.arrivalId = builder.arrivalId;
    this.originId = builder.originId;
  }

  @Column(name = "arid", nullable = false)
  public long getArrivalId() {
    return arrivalId;
  }

  public void setArrivalId(long arrivalId) {
    this.arrivalId = arrivalId;
  }

  @Column(name = "orid", nullable = false)
  public long getOriginId() {
    return originId;
  }

  public void setOriginId(long originId) {
    this.originId = originId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AridOridKey that = (AridOridKey) o;
    return arrivalId == that.arrivalId && originId == that.originId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(arrivalId, originId);
  }

  public static class Builder {
    private long arrivalId;
    private long originId;

    public Builder withArrivalId(long arrivalId) {
      this.arrivalId = arrivalId;
      return this;
    }

    public Builder withOriginId(long originId) {
      this.originId = originId;
      return this;
    }

    public AridOridKey build() {
      return new AridOridKey(this);
    }
  }
}
