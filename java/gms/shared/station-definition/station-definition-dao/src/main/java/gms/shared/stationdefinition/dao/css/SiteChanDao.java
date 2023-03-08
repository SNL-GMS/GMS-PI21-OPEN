package gms.shared.stationdefinition.dao.css;

import gms.shared.stationdefinition.dao.css.converter.ChannelTypeConverter;
import gms.shared.stationdefinition.dao.css.enums.ChannelType;
import gms.shared.utilities.bridge.database.converter.JulianDateConverterPositiveNa;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "sitechan")
public class SiteChanDao {

  @EmbeddedId
  private SiteChanKey id;

  @Column(name = "chanid")
  private long channelId;

  @Column(name = "offdate")
  @Convert(converter = JulianDateConverterPositiveNa.class)
  private Instant offDate;

  @Column(name = "ctype")
  @Convert(converter = ChannelTypeConverter.class)
  private ChannelType channelType;

  @Column(name = "edepth")
  private double emplacementDepth;

  @Column(name = "hang")
  private double horizontalAngle;

  @Column(name = "vang")
  private double verticalAngle;

  @Column(name = "descrip")
  private String channelDescription;

  @Column(name = "lddate")
  private Instant loadDate;

  public SiteChanDao() {

    // JPA constructor

  }

  public SiteChanDao(SiteChanDao siteChanDao){

    var siteChanKey = siteChanDao.id;
    this.id = new SiteChanKey(siteChanKey.getStationCode(), siteChanKey.getChannelCode(), siteChanKey.getOnDate());
    this.channelId = siteChanDao.channelId;
    this.offDate = siteChanDao.offDate;
    this.channelType = siteChanDao.channelType;
    this.emplacementDepth = siteChanDao.emplacementDepth;
    this.horizontalAngle = siteChanDao.horizontalAngle;
    this.verticalAngle = siteChanDao.verticalAngle;
    this.channelDescription = siteChanDao.channelDescription;
    this.loadDate = siteChanDao.loadDate;
  }

  public SiteChanKey getId() {
    return id;
  }

  public void setId(SiteChanKey id) {
    this.id = id;
  }

  public long getChannelId() {
    return channelId;
  }

  public void setChannelId(long channelId) {
    this.channelId = channelId;
  }

  public Instant getOffDate() {
    return offDate;
  }

  public void setOffDate(Instant offDate) {
    this.offDate = offDate;
  }

  public ChannelType getChannelType() {
    return channelType;
  }

  public void setChannelType(ChannelType channelType) {
    this.channelType = channelType;
  }

  public double getEmplacementDepth() {
    return emplacementDepth;
  }

  public void setEmplacementDepth(double emplacementDepth) {
    this.emplacementDepth = emplacementDepth;
  }

  public double getHorizontalAngle() {
    return horizontalAngle;
  }

  public void setHorizontalAngle(double horizontalAngle) {
    this.horizontalAngle = horizontalAngle;
  }

  public double getVerticalAngle() {
    return verticalAngle;
  }

  public void setVerticalAngle(double verticalAngle) {
    this.verticalAngle = verticalAngle;
  }

  public String getChannelDescription() {
    return channelDescription;
  }

  public void setChannelDescription(String channelDescription) {
    this.channelDescription = channelDescription;
  }

  public Instant getLoadDate() {
    return loadDate;
  }

  public void setLoadDate(Instant loadDate) {
    this.loadDate = loadDate;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SiteChanDao that = (SiteChanDao) o;
    return id.equals(that.id) && offDate.equals(that.offDate) && channelId == that.channelId
      && channelType.equals(that.channelType) && emplacementDepth == that.emplacementDepth
      && horizontalAngle == that.horizontalAngle && verticalAngle == that.verticalAngle
      && channelDescription.equals(that.channelDescription) && loadDate.equals(that.loadDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, offDate, channelId, channelType, emplacementDepth, horizontalAngle,
      verticalAngle, channelDescription, loadDate);
  }
}
