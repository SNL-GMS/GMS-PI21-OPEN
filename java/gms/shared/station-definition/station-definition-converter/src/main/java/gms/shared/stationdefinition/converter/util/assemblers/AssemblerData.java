package gms.shared.stationdefinition.converter.util.assemblers;

import com.google.common.collect.Table;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.dao.css.SiteChanDao;
import gms.shared.stationdefinition.dao.css.SiteDao;

import java.time.Instant;
import java.util.List;
import java.util.NavigableMap;

public class AssemblerData {
  private SiteDao siteDao;
  private List<SiteChanDao> siteChans;
  private Instant effectiveAt;
  private Table<String, String, NavigableMap<Instant, Channel>> channelsByStaChan;

  public AssemblerData(final SiteDao siteDao, final List<SiteChanDao> siteChans, final Instant effectiveAt,
    Table<String, String, NavigableMap<Instant, Channel>> channelsByStaChan) {
    this.siteDao = siteDao;
    this.siteChans = siteChans;
    this.effectiveAt = effectiveAt;
    this.channelsByStaChan = channelsByStaChan;
  }

  public SiteDao getSiteDao() {
    return this.siteDao;
  }

  public List<SiteChanDao> getSiteChans() {
    return this.siteChans;
  }

  public Instant getEffectiveAt() {
    return this.effectiveAt;
  }

  public Table<String, String, NavigableMap<Instant, Channel>> getChannelsByStaChan(){ return channelsByStaChan; }
}
