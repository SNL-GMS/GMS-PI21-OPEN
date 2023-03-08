package gms.shared.workflow.dao;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;
import java.util.Objects;

/**
 * Represents an interval record in the `interval` legacy table.
 */
@Entity
@Table(name = "interval")
public class IntervalDao {

  private ClassEndTimeNameTimeKey classEndTimeNameTimeKey;

  private long intervalIdentifier;

  private String state;

  private String author;

  private double percentAvailable;

  private Instant processStartDate;

  private Instant processEndDate;

  private Instant lastModificationDate;

  private Instant loadDate;

  public static final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
    .appendPattern("dd")
    .appendLiteral('-')
    .appendPattern("MMM")
    .appendLiteral('-')
    .appendPattern("yyyy")
    .appendLiteral(' ')
    .appendPattern("HH")
    .optionalStart().appendLiteral(':').optionalEnd()
    .appendPattern("mm")
    .optionalStart().appendLiteral(':').optionalEnd()
    .appendPattern("ss")
    .optionalStart().appendPattern("X").optionalEnd()
    .toFormatter();

  public IntervalDao() {
    // JPA constructor
  }

  @Column(name = "intvlid")
  public long getIntervalIdentifier() {
    return intervalIdentifier;
  }

  public void setIntervalIdentifier(long intervalIdentifier) {
    this.intervalIdentifier = intervalIdentifier;
  }

  @EmbeddedId
  public ClassEndTimeNameTimeKey getClassEndTimeNameTimeKey() {
    return classEndTimeNameTimeKey;
  }

  public void setClassEndTimeNameTimeKey(ClassEndTimeNameTimeKey classEndTimeNameTimeKey) {
    this.classEndTimeNameTimeKey = classEndTimeNameTimeKey;
  }

  @Transient
  public String getType() {
    return getClassEndTimeNameTimeKey().getType();
  }

  @Transient
  public String getName() {
    return getClassEndTimeNameTimeKey().getName();
  }

  @Transient
  public double getTime() {
    return getClassEndTimeNameTimeKey().getTime();
  }

  @Transient
  public double getEndTime() {
    return getClassEndTimeNameTimeKey().getEndTime();
  }

  @Column
  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  @Column(name = "auth")
  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  @Column(name = "percent_available")
  public double getPercentAvailable() {
    return percentAvailable;
  }

  public void setPercentAvailable(double percentAvailable) {
    this.percentAvailable = percentAvailable;
  }

  @Column(name = "proc_start_date")
  public Instant getProcessStartDate() {
    return processStartDate;
  }

  public void setProcessStartDate(Instant processStartDate) {
    this.processStartDate = processStartDate;
  }

  @Column(name = "proc_end_date")
  public Instant getProcessEndDate() {
    return processEndDate;
  }

  public void setProcessEndDate(Instant processEndDate) {
    this.processEndDate = processEndDate;
  }

  @Column(name = "moddate")
  public Instant getLastModificationDate() {
    return lastModificationDate;
  }

  public void setLastModificationDate(Instant lastModificationDate) {
    this.lastModificationDate = lastModificationDate;
  }

  @Column(name = "lddate")
  public Instant getLoadDate() {
    return loadDate;
  }

  public void setLoadDate(Instant loadDate) {
    this.loadDate = loadDate;
  }

  public static Instant convertTableDate(String tableData) {

    var formattedString = tableData.toLowerCase(Locale.ENGLISH);
    formattedString = formattedString.replace(formattedString.charAt(3),
      Character.toUpperCase(formattedString.charAt(3)));

    var ldt = LocalDateTime.parse(formattedString, formatter);

    return ldt.toInstant(ZoneOffset.ofHours(0));

  }

  private IntervalDao(Builder builder) {

    this.classEndTimeNameTimeKey = new ClassEndTimeNameTimeKey(builder.type, builder.name, builder.endTime, builder.time);
    this.intervalIdentifier = builder.intervalIdentifier;
    this.state = builder.state;
    this.author = builder.author;
    this.percentAvailable = builder.percentAvailable;
    this.processStartDate = builder.processStartDate;
    this.processEndDate = builder.processEndDate;
    this.lastModificationDate = builder.lastModificationDate;
    this.loadDate = builder.loadDate;
  }

  public static class Builder {

    private long intervalIdentifier;
    private String type;
    private String name;
    private double time;
    private double endTime;
    private String state;
    private String author;
    private double percentAvailable;
    private Instant processStartDate;
    private Instant processEndDate;
    private Instant lastModificationDate;
    private Instant loadDate;

    public Builder intervalIdentifier(long intervalIdentifier) {
      this.intervalIdentifier = intervalIdentifier;
      return this;
    }

    public Builder type(String type) {
      this.type = type;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder time(double time) {
      this.time = time;
      return this;
    }

    public Builder endTime(double endTime) {
      this.endTime = endTime;
      return this;
    }

    public Builder state(String state) {
      this.state = state;
      return this;
    }

    public Builder author(String author) {
      this.author = author;
      return this;
    }

    public Builder percentAvailable(double percentAvailable) {
      this.percentAvailable = percentAvailable;
      return this;
    }

    public Builder processStartDate(Instant processStartDate) {
      this.processStartDate = processStartDate;
      return this;
    }

    public Builder processEndDate(Instant processEndDate) {
      this.processEndDate = processEndDate;
      return this;
    }

    public Builder lastModificationDate(Instant lastModificationDate) {
      this.lastModificationDate = lastModificationDate;
      return this;
    }

    public Builder loadDate(Instant loadDate) {
      this.loadDate = loadDate;
      return this;
    }

    public IntervalDao build() {
      return new IntervalDao(this);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    IntervalDao that = (IntervalDao) o;

    return intervalIdentifier == that.intervalIdentifier
      && Double.compare(that.percentAvailable, percentAvailable) == 0 && Objects
      .equals(classEndTimeNameTimeKey, that.classEndTimeNameTimeKey) && Objects
      .equals(state, that.state) && Objects.equals(author, that.author)
      && Objects.equals(processStartDate, that.processStartDate) && Objects
      .equals(processEndDate, that.processEndDate) && Objects
      .equals(lastModificationDate, that.lastModificationDate) && Objects
      .equals(loadDate, that.loadDate);
  }

  @Override
  public String toString() {
    return "IntervalDao{" +
      "classEndTimeNameTimeKey=" + classEndTimeNameTimeKey +
      ", intervalIdentifier=" + intervalIdentifier +
      ", state='" + state + '\'' +
      ", author='" + author + '\'' +
      ", percentAvailable=" + percentAvailable +
      ", processStartDate=" + processStartDate +
      ", processEndDate=" + processEndDate +
      ", lastModificationDate=" + lastModificationDate +
      ", loadDate=" + loadDate +
      '}';
  }

  @Override
  public int hashCode() {
    return Objects
      .hash(classEndTimeNameTimeKey, intervalIdentifier, state, author, percentAvailable,
        processStartDate, processEndDate, lastModificationDate, loadDate);


  }
}
