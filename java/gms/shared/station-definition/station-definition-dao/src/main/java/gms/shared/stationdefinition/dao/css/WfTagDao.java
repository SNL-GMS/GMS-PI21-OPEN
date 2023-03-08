package gms.shared.stationdefinition.dao.css;

import org.apache.commons.lang3.Validate;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "wftag")
public class WfTagDao {

  private WfTagKey wfTagKey;
  private Instant loadDate;

  public WfTagDao() {
    //JPA Constructor
  }

  /**
   * Return a deep copy of the given {@link WfTagDao}
   *
   * @param wfTagDao WfTagDao to copy
   */
  public WfTagDao(WfTagDao wfTagDao) {

    Validate.notNull(wfTagDao);
    Validate.notNull(wfTagDao.getWfTagKey());

    this.loadDate = wfTagDao.loadDate;
    this.wfTagKey = new WfTagKey();
    var oldKey = wfTagDao.getWfTagKey();
    this.wfTagKey.setId(oldKey.getId());
    this.wfTagKey.setTagName(oldKey.getTagName());
    this.wfTagKey.setWfId(oldKey.getWfId());
  }

  @EmbeddedId
  public WfTagKey getWfTagKey() {
    return wfTagKey;
  }

  public void setWfTagKey(WfTagKey wfTagKey) {
    this.wfTagKey = wfTagKey;
  }

  @Column(name = "lddate", nullable = false)
  public Instant getLoadDate() {
    return loadDate;
  }

  public void setLoadDate(Instant loadDate) {
    this.loadDate = loadDate;
  }


}
