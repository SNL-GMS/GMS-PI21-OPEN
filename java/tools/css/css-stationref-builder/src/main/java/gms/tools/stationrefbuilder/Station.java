package gms.tools.stationrefbuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The data object for the Station Metadata manipulator.
 */
class Station {

  private String stationName;
  @JsonIgnore
  private String imsNum;
  @JsonIgnore
  private String location;
  @JsonIgnore
  private String type;
  @JsonIgnore
  private int chanNum;
  @JsonIgnore
  private List<Channel> channels;
  @JsonIgnore
  private String format;
  @JsonIgnore
  private String comments;
  @JsonIgnore
  private String priority;
  @JsonIgnore
  private String dateStarted;
  @JsonIgnore
  private String continent;
  @JsonIgnore
  private Map<String, Boolean> inGroups;

  //These should be picked up by Jackson
  private boolean isAcquired = true;
  private boolean frameProcessingDisable = false;
  private int portOffset;

  /**
   * Public default constructor for Station object.
   */
  public Station() {

  }

  /**
   * Constructor requiring the station's name.
   */
  Station(String stationName) {
    this.stationName = stationName;
    inGroups = new HashMap<>();
  }

  Station(String stationName, int offset) {
    this.stationName = stationName;
    this.portOffset = offset;
  }

  /**
   * Setter for station Name.
   */
  public void setStationName(String stationName) {
    this.stationName = stationName;
  }

  /**
   * Setter for IMS Number (currently not utilized)
   */
  public void setImsNum(String imsNum) {
    this.imsNum = imsNum;
  }

  /**
   * Setter for the Location.
   */
  public void setLocation(String location) {
    this.location = location;
  }

  /**
   * Setter for the Type.
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Setter for the number of active channels.
   */
  public void setChanNum(int chanNum) {
    this.chanNum = chanNum;
  }

  /**
   * Setter for the station format.
   */
  public void setFormat(String format) {
    this.format = format;
  }

  /**
   * Setter for any comments on a station.
   */
  public void setComments(String comments) {
    this.comments = comments;
  }

  /**
   * Setter for the channels.
   */
  public void setChannels(List<Channel> channels) {
    this.channels = channels;
  }

  /**
   * Setter for priority.
   */
  public void setPriority(String priority) {
    this.priority = priority;
  }

  /**
   * Setter for the stationStartTime
   */
  public void setDateStarted(String dateStarted) {
    this.dateStarted = dateStarted;
    if (!dateStarted.contains(".")) {
      this.dateStarted += ".00000";
    }
  }

  /**
   * Setter for the Group Map.
   */
  public void setInGroups(Map<String, Boolean> inGrp) {
    inGroups = inGrp;
  }

  /**
   * Setter for continent affiliation.
   */
  public void setContinent(String cont) {
    continent = cont;
  }

  /**
   * Setter for PortOffset
   */
  public void setPortOffset(int portOffset) {
    this.portOffset = portOffset;
  }

  /**
   * Getter for continent affiliation.
   *
   * @return The Continent deduced by location/country.
   */
  public String getContinent() {
    return continent;
  }

  /**
   * Getter for station name.
   *
   * @return The Station Name
   */
  public String getStationName() {
    return stationName;
  }

  /**
   * Getter for IMS Number, not used yet.
   *
   * @return the IMS Number
   */
  public String getImsNum() {
    if (imsNum == null) {
      imsNum = "";
    }
    return imsNum;
  }

  /**
   * Getter for the location
   *
   * @return the location
   */
  public String getLocation() {
    return location;
  }

  /**
   * Getter for the station type
   *
   * @return the type
   */
  public String getType() {
    if (type == null) {
      type = "";
    }
    return type;
  }

  /**
   * Getter for the number of channels
   *
   * @return channel numbers
   */
  public int getChanNum() {
    return chanNum;
  }

  /**
   * Getter for the station format
   *
   * @return format
   */
  public String getFormat() {
    if (format == null) {
      format = "";
    }
    return format;
  }

  /**
   * Getter for comments
   *
   * @return the comments
   */
  public String getComments() {
    if (comments == null) {
      comments = "";
    }
    return comments;
  }

  /**
   * Getter for channel list
   *
   * @return the list of channels by name
   */
  public List<Channel> getChannels() {
    return channels;
  }

  /**
   * Getter for start Date/Time in Seconds
   *
   * @return dateStarted
   */
  public String getDateStarted() {
    return dateStarted;
  }

  /**
   * Getter for Group Map.
   *
   * @return the map of groups key - group, isMember - true/false
   */
  public Map<String, Boolean> getInGroups() {
    return inGroups;
  }

  /**
   * Getter for priority.
   *
   * @return the priority of the station.
   */
  public String getPriority() {
    return priority;
  }

  /**
   * Getter for isAcquired
   *
   * @return whether the station is acquired.
   */
  public boolean isAcquired() {
    return isAcquired;
  }

  /**
   * Getter for isFrameProcessingDisabled
   *
   * @return whether frame processing is disabled for this station
   */
  public boolean isFrameProcessingDisable() {
    return frameProcessingDisable;
  }

  /**
   * Getter for port offset
   * The port offset for the station, for dataman.
   */
  public int getPortOffset() {
    return portOffset;
  }

  /**
   * Overrides the toString method to correctly display the station information in the following
   * format (for generating the CSV):
   */
  @Override
  public String toString() {
    return String
      .format("%s\t%s\t%s\t%s\t%s\t%d\t%s\t%s%n", getStationName(), getImsNum(), getLocation(), getContinent(),
        getType(), getChanNum(), getFormat(), getComments());
  }


}
