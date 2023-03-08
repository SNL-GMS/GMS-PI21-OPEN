package gms.dataacquisition.cssreader.data;

import com.github.ffpojo.metadata.positional.annotation.PositionalField;
import com.github.ffpojo.metadata.positional.annotation.PositionalRecord;
import gms.dataacquisition.cssreader.utilities.CssReaderUtility;

import java.time.Instant;

@PositionalRecord
public class NetworkRecordP3 extends NetworkRecord {
  private static final int RECORD_LENGTH = 138;

  public static int getRecordLength() {
    return RECORD_LENGTH;
  }

  @Override
  @PositionalField(initialPosition = 1, finalPosition = 8)
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name.trim();
  }

  @Override
  @PositionalField(initialPosition = 10, finalPosition = 89)
  public String getDesc() {
    return desc;
  }

  public void setDesc(String desc) {
    this.desc = desc.trim();
  }

  @Override
  @PositionalField(initialPosition = 91, finalPosition = 94)
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type.trim();
  }

  @Override
  @PositionalField(initialPosition = 96, finalPosition = 110)
  public String getAuth() {
    return auth;
  }

  public void setAuth(String auth) {
    this.auth = auth.trim();
  }

  @Override
  @PositionalField(initialPosition = 112, finalPosition = 120)
  public int getCommentId() {
    return commentId;
  }

  public void setCommentId(String commentId) {
    this.commentId = Integer.valueOf(commentId);
  }

  @Override
  @PositionalField(initialPosition = 122, finalPosition = 138)
  public Instant getLddate() {
    return lddate;
  }

  public void setLddate(String lddate) {
    this.lddate = CssReaderUtility.parseDate(lddate).orElse(null);
  }


}
