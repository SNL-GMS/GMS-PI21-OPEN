package gms.dataacquisition.cssreader.data;

import com.github.ffpojo.metadata.positional.annotation.PositionalField;
import com.github.ffpojo.metadata.positional.annotation.PositionalRecord;
import gms.dataacquisition.cssreader.utilities.CssReaderUtility;

import java.time.Instant;
import java.util.Optional;

@PositionalRecord
public class AffiliationRecordCss30 extends AffiliationRecord {

  public static final int RECORD_LENGTH = 33;

  @Override
  @PositionalField(initialPosition = 1, finalPosition = 8)
  public String getNet() {
    return this.net;
  }

  public void setNet(String s) {
    this.net = s.trim();
  }

  @Override
  @PositionalField(initialPosition = 10, finalPosition = 15)
  public String getSta() {
    return this.sta;
  }

  public void setSta(String s) {
    this.sta = s.trim();
  }

  @Override
  @PositionalField(initialPosition = 17, finalPosition = 33)
  public Optional<Instant> getLddate() {
    return lddate;
  }

  public void setLddate(String s) {
    this.lddate = CssReaderUtility.parseDate(s);
  }
}