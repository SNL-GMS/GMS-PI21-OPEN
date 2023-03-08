package gms.shared.stationdefinition.repository.util;

import com.google.auto.value.AutoValue;
import gms.shared.stationdefinition.dao.css.enums.TagName;

@AutoValue
public abstract class DerivedChannelIdComponents {

  public abstract TagName getAssociatedRecordType();

  public abstract long getAssociatedRecordId();

  public abstract long getWfid();


  public static DerivedChannelIdComponents create(TagName tagName, long recordId, long wfid) {

    return new AutoValue_DerivedChannelIdComponents(tagName, recordId, wfid);
  }

}