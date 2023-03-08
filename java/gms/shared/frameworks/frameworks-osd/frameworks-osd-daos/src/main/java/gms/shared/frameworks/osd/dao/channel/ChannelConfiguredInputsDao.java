package gms.shared.frameworks.osd.dao.channel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "channel_configured_inputs")
public class ChannelConfiguredInputsDao {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "channel_configured_inputs_sequence")
  @SequenceGenerator(name = "channel_configured_inputs_sequence", sequenceName = "channel_configured_inputs_sequence", allocationSize = 5)
  private int id;

  @ManyToOne
  @JoinColumn(
    referencedColumnName = "name",
    name = "channel_name",
    nullable = false
  )
  private ChannelDao channel;

  @ManyToOne
  @JoinColumn(
    referencedColumnName = "name",
    name = "related_channel_name",
    nullable = false
  )
  private ChannelDao relatedChannel;

  @Column(name = "channel_name", insertable = false, updatable = false)
  private String channelName;

  @Column(name = "related_channel_name", insertable = false, updatable = false)
  private String relatedChannelName;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public ChannelDao getChannel() {
    return channel;
  }

  public void setChannel(
    ChannelDao channel) {
    this.channel = channel;
  }

  public ChannelDao getRelatedChannel() {
    return relatedChannel;
  }

  public void setRelatedChannel(
    ChannelDao relatedChannel) {
    this.relatedChannel = relatedChannel;
  }

  public String getChannelName() {
    return channelName;
  }

  public void setChannelName(String channelName) {
    this.channelName = channelName;
  }

  public String getRelatedChannelName() {
    return relatedChannelName;
  }

  public void setRelatedChannelName(String relatedChannelName) {
    this.relatedChannelName = relatedChannelName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ChannelConfiguredInputsDao that = (ChannelConfiguredInputsDao) o;
    return id == that.id &&
      channel.equals(that.channel) &&
      relatedChannel.equals(that.relatedChannel) &&
      channelName.equals(that.channelName) &&
      relatedChannelName.equals(that.relatedChannelName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, channel, relatedChannel, channelName, relatedChannelName);
  }

  @Override
  public String toString() {
    return "ChannelConfiguredInputsDao{" +
      "id=" + id +
      ", channel=" + channel +
      ", relatedChannel=" + relatedChannel +
      ", channelName='" + channelName + '\'' +
      ", relatedChannelName='" + relatedChannelName + '\'' +
      '}';
  }
}
