package gms.shared.stationdefinition.dao.css;

import gms.shared.stationdefinition.dao.css.converter.NetworkTypeConverter;
import gms.shared.stationdefinition.dao.css.enums.NetworkType;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "network")
public class NetworkDao {

  private long networkId;
  private String net;
  private String networkName;
  private String description;
  private NetworkType networkType;
  private Instant onDate;
  private Instant offDate;
  private String author;
  private Instant modDate;
  private Instant ldDate;

  public NetworkDao() {
    // JPA constructor
  }

  @Id
  @Column(name = "networkid")
  public long getNetworkId() {
    return this.networkId;
  }

  public void setNetworkId(final long networkId) {
    this.networkId = networkId;
  }

  @Column(name = "net")
  public String getNet() {
    return net;
  }

  public void setNet(String net) {
    this.net = net;
  }

  @Column(name = "network_name")
  public String getNetworkName() {
    return networkName;
  }

  public void setNetworkName(String networkName) {
    this.networkName = networkName;
  }

  @Column(name = "description")
  public String getDescription() {
    return this.description;
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  @Column(name = "network_type")
  @Convert(converter = NetworkTypeConverter.class)
  public NetworkType getNetworkType() {
    return networkType;
  }

  public void setNetworkType(NetworkType networkType) {
    this.networkType = networkType;
  }

  @Column(name = "on_date")
  public Instant getOnDate() {
    return onDate;
  }

  public void setOnDate(Instant onDate) {
    this.onDate = onDate;
  }

  @Column(name = "off_date")
  public Instant getOffDate() {
    return offDate;
  }

  public void setOffDate(Instant offDate) {
    this.offDate = offDate;
  }

  @Column(name = "author")
  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  @Column(name = "moddate")
  public Instant getModDate() {
    return modDate;
  }

  public void setModDate(Instant modDate) {
    this.modDate = modDate;
  }

  @Column(name = "lddate")
  public Instant getLdDate() {
    return ldDate;
  }

  public void setLdDate(Instant ldDate) {
    this.ldDate = ldDate;
  }
}
