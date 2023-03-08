package gms.shared.event.dao;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Aggregates an eventId and an originId into a single object
 */
@Embeddable
public class EventIdOriginIdKey implements Serializable {

  private long eventId;

  private long originId;

  public EventIdOriginIdKey() {

  }

  private EventIdOriginIdKey(Builder builder) {
    this.eventId = builder.eventId;
    this.originId = builder.originId;
  }

  @Column(name = "evid")
  public long getEventId() {
    return eventId;
  }

  public void setEventId(long eventId) {
    this.eventId = eventId;
  }

  @Column(name = "orid")
  public long getOriginId() {
    return originId;
  }

  public void setOriginId(long originId) {
    this.originId = originId;
  }

  public static class Builder {

    private long eventId;
    private long originId;

    public Builder withEventId(long eventId) {
      this.eventId = eventId;
      return this;
    }

    public Builder withOriginId(long originId) {
      this.originId = originId;
      return this;
    }

    public EventIdOriginIdKey build() {
      return new EventIdOriginIdKey(this);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EventIdOriginIdKey)) {
      return false;
    }
    EventIdOriginIdKey that = (EventIdOriginIdKey) o;
    return eventId == that.eventId && originId == that.originId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(eventId, originId);
  }

  @Override
  public String toString() {
    return "EventIdOriginIdKey{" +
      "eventId=" + eventId +
      ", originId=" + originId +
      '}';
  }
}
