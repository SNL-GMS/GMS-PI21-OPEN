package gms.shared.workflow.dao;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Represents the primary key into the interval record from the `interval` legacy table.
 */
@Embeddable
public class ClassEndTimeNameTimeKey implements Serializable {

  //constraint INTERVAL_PK
  //primary key (CLASS, ENDTIME, NAME, TIME)
  private String type;
  private String name;
  private double endTime;
  private double time;

  public ClassEndTimeNameTimeKey() {
  }

  public ClassEndTimeNameTimeKey(String type, String name, double endTime, double time) {
    this.type = type;
    this.name = name;
    this.endTime = endTime;
    this.time = time;
  }

  @Column(name = "class")
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Column
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Column
  public double getTime() {
    return time;
  }

  public void setTime(double time) {
    this.time = time;
  }

  @Column(name = "endtime")
  public double getEndTime() {
    return endTime;
  }

  public void setEndTime(double endTime) {
    this.endTime = endTime;
  }

  //constraint INTERVAL_PK
  //primary key (CLASS, ENDTIME, NAME, TIME)
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ClassEndTimeNameTimeKey that = (ClassEndTimeNameTimeKey) o;
    return getType().equals(that.getType()) &&
      getName().equals(that.getName()) &&
      getEndTime() == (that.getEndTime()) &&
      getTime() == that.getTime();
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, name, endTime, time);
  }

  @Override
  public String toString() {
    return "ClassEndTimeNameTimeKey{" +
      "type='" + type + '\'' +
      ", name='" + name + '\'' +
      ", endTime=" + endTime +
      ", time=" + time +
      '}';
  }
}
