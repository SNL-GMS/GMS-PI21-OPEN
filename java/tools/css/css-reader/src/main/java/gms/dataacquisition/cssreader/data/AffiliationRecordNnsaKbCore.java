package gms.dataacquisition.cssreader.data;

import com.github.ffpojo.metadata.positional.annotation.PositionalField;
import com.github.ffpojo.metadata.positional.annotation.PositionalRecord;
import gms.dataacquisition.cssreader.utilities.CssReaderUtility;
import org.apache.commons.lang3.Validate;

import java.time.Instant;
import java.util.Optional;

@PositionalRecord
public class AffiliationRecordNnsaKbCore extends AffiliationRecord {

  public static final int RECORD_LENGTH = 69;

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
  public Optional<Instant> getTime() {
    return this.time;
  }

  public void setTime(String s) {
    this.time = CssReaderUtility.toInstant(s);
  }

  @Override
  @PositionalField(initialPosition = 35, finalPosition = 51)
  public Optional<Instant> getEndtime() {
    return endtime;
  }

  public void setEndtime(String s) {
    this.endtime = CssReaderUtility.toInstant(s);
  }

  @Override
  @PositionalField(initialPosition = 53, finalPosition = 69)
  public Optional<Instant> getLddate() {
    return lddate;
  }

  public void setLddate(String s) {
    this.lddate = CssReaderUtility.parseDate(s);
  }

  @Override
  public void validate() {
    Validate.notEmpty(getNet(), "NET field is empty");
    Validate.notEmpty(getSta(), "STA field is empty");
    Validate.notNull(getLddate(), "LDDATE field is null");

    Optional<Instant> endtime = getEndtime();
    Optional<Instant> time = getTime();

    if (endtime.isPresent() && time.isPresent() && time.get().getEpochSecond() > endtime.get()
      .getEpochSecond()) {
      throw new IllegalArgumentException("TIME is greater than ENDTIME");
    }
  }
}
