package gms.shared.event.dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents an origerr record in the `origerr` legacy table.
 */
@Entity
@Table(name = "origerr")
public class OrigerrDao {

  private long originId;
  private double covarianceMatrixSxx;
  private double covarianceMatrixSyy;
  private double covarianceMatrixSzz;
  private double covarianceMatrixStt;
  private double covarianceMatrixSxy;
  private double covarianceMatrixSxz;
  private double covarianceMatrixSyz;
  private double covarianceMatrixStx;
  private double covarianceMatrixSty;
  private double covarianceMatrixStz;
  private double standardErrorOfObservations;
  private double semiMajorAxisOfError;
  private double semiMinorAxisOfError;
  private double strikeOfSemiMajorAxis;
  private double depthError;
  private double originTimeError;
  private double confidence;
  private long commentId;
  private Instant loadDate;

  @Id
  @Column(name = "orid")
  public long getOriginId() {
    return originId;
  }

  public void setOriginId(long originId) {
    this.originId = originId;
  }

  @Column(name = "sxx")
  public double getCovarianceMatrixSxx() {
    return covarianceMatrixSxx;
  }


  public void setCovarianceMatrixSxx(double covarianceMatrixSxx) {
    this.covarianceMatrixSxx = covarianceMatrixSxx;
  }

  @Column(name = "syy")
  public double getCovarianceMatrixSyy() {
    return covarianceMatrixSyy;
  }

  public void setCovarianceMatrixSyy(double covarianceMatrixSyy) {
    this.covarianceMatrixSyy = covarianceMatrixSyy;
  }

  @Column(name = "szz")
  public double getCovarianceMatrixSzz() {
    return covarianceMatrixSzz;
  }

  public void setCovarianceMatrixSzz(double covarianceMatrixSzz) {
    this.covarianceMatrixSzz = covarianceMatrixSzz;
  }

  @Column(name = "stt")
  public double getCovarianceMatrixStt() {
    return covarianceMatrixStt;
  }

  public void setCovarianceMatrixStt(double covarianceMatrixStt) {
    this.covarianceMatrixStt = covarianceMatrixStt;
  }

  @Column(name = "sxy")
  public double getCovarianceMatrixSxy() {
    return covarianceMatrixSxy;
  }

  public void setCovarianceMatrixSxy(double covarianceMatrixSxy) {
    this.covarianceMatrixSxy = covarianceMatrixSxy;
  }

  @Column(name = "sxz")
  public double getCovarianceMatrixSxz() {
    return covarianceMatrixSxz;
  }

  public void setCovarianceMatrixSxz(double covarianceMatrixSxz) {
    this.covarianceMatrixSxz = covarianceMatrixSxz;
  }

  @Column(name = "syz")
  public double getCovarianceMatrixSyz() {
    return covarianceMatrixSyz;
  }

  public void setCovarianceMatrixSyz(double covarianceMatrixSyz) {
    this.covarianceMatrixSyz = covarianceMatrixSyz;
  }

  @Column(name = "stx")
  public double getCovarianceMatrixStx() {
    return covarianceMatrixStx;
  }

  public void setCovarianceMatrixStx(double covarianceMatrixStx) {
    this.covarianceMatrixStx = covarianceMatrixStx;
  }

  @Column(name = "sty")
  public double getCovarianceMatrixSty() {
    return covarianceMatrixSty;
  }

  public void setCovarianceMatrixSty(double covarianceMatrixSty) {
    this.covarianceMatrixSty = covarianceMatrixSty;
  }

  @Column(name = "stz")
  public double getCovarianceMatrixStz() {
    return covarianceMatrixStz;
  }

  public void setCovarianceMatrixStz(double covarianceMatrixStz) {
    this.covarianceMatrixStz = covarianceMatrixStz;
  }

  @Column(name = "sdobs")
  public double getStandardErrorOfObservations() {
    return standardErrorOfObservations;
  }

  public void setStandardErrorOfObservations(double standardErrorOfObservations) {
    this.standardErrorOfObservations = standardErrorOfObservations;
  }

  @Column(name = "smajax")
  public double getSemiMajorAxisOfError() {
    return semiMajorAxisOfError;
  }

  public void setSemiMajorAxisOfError(double semiMajorAxisOfError) {
    this.semiMajorAxisOfError = semiMajorAxisOfError;
  }

  @Column(name = "sminax")
  public double getSemiMinorAxisOfError() {
    return semiMinorAxisOfError;
  }

  public void setSemiMinorAxisOfError(double semiMinorAxisOfError) {
    this.semiMinorAxisOfError = semiMinorAxisOfError;
  }

  @Column(name = "strike")
  public double getStrikeOfSemiMajorAxis() {
    return strikeOfSemiMajorAxis;
  }

  public void setStrikeOfSemiMajorAxis(double strikeOfSemiMajorAxis) {
    this.strikeOfSemiMajorAxis = strikeOfSemiMajorAxis;
  }

  @Column(name = "sdepth")
  public double getDepthError() {
    return depthError;
  }

  public void setDepthError(double depthError) {
    this.depthError = depthError;
  }

  @Column(name = "stime")
  public double getOriginTimeError() {
    return originTimeError;
  }

  public void setOriginTimeError(double originTimeError) {
    this.originTimeError = originTimeError;
  }

  @Column(name = "conf")
  public double getConfidence() {
    return confidence;
  }

  public void setConfidence(double confidence) {
    this.confidence = confidence;
  }

  @Column(name = "commid")
  public long getCommentId() {
    return commentId;
  }

  public void setCommentId(long commentId) {
    this.commentId = commentId;
  }

  @Column(name = "lddate")
  public Instant getLoadDate() {
    return loadDate;
  }

  public void setLoadDate(Instant loadDate) {
    this.loadDate = loadDate;
  }


  public OrigerrDao() {

  }

  private OrigerrDao(OrigerrDao.Builder builder) {

    this.originId = builder.originId;
    this.covarianceMatrixSxx = builder.covarianceMatrixSxx;
    this.covarianceMatrixSyy = builder.covarianceMatrixSyy;
    this.covarianceMatrixSzz = builder.covarianceMatrixSzz;
    this.covarianceMatrixStt = builder.covarianceMatrixStt;
    this.covarianceMatrixSxy = builder.covarianceMatrixSxy;
    this.covarianceMatrixSxz = builder.covarianceMatrixSxz;
    this.covarianceMatrixSyz = builder.covarianceMatrixSyz;
    this.covarianceMatrixStx = builder.covarianceMatrixStx;
    this.covarianceMatrixSty = builder.covarianceMatrixSty;
    this.covarianceMatrixStz = builder.covarianceMatrixStz;
    this.standardErrorOfObservations = builder.standardErrorOfObservations;
    this.semiMajorAxisOfError = builder.semiMajorAxisOfError;
    this.semiMinorAxisOfError = builder.semiMinorAxisOfError;
    this.strikeOfSemiMajorAxis = builder.strikeOfSemiMajorAxis;
    this.depthError = builder.depthError;
    this.originTimeError = builder.originTimeError;
    this.confidence = builder.confidence;
    this.commentId = builder.commentId;
    this.loadDate = builder.loadDate;

  }

  public static class Builder {

    private long originId;
    private double covarianceMatrixSxx;
    private double covarianceMatrixSyy;
    private double covarianceMatrixSzz;
    private double covarianceMatrixStt;
    private double covarianceMatrixSxy;
    private double covarianceMatrixSxz;
    private double covarianceMatrixSyz;
    private double covarianceMatrixStx;
    private double covarianceMatrixSty;
    private double covarianceMatrixStz;
    private double standardErrorOfObservations;
    private double semiMajorAxisOfError;
    private double semiMinorAxisOfError;
    private double strikeOfSemiMajorAxis;
    private double depthError;
    private double originTimeError;
    private double confidence;
    private long commentId;
    private Instant loadDate;

    public static Builder initializeFromInstance(OrigerrDao origerr) {
      return new OrigerrDao.Builder()
        .withOriginId(origerr.originId)
        .withCovarianceMatrixSxx(origerr.covarianceMatrixSxx)
        .withCovarianceMatrixSyy(origerr.covarianceMatrixSyy)
        .withCovarianceMatrixSzz(origerr.covarianceMatrixSzz)
        .withCovarianceMatrixStt(origerr.covarianceMatrixStt)
        .withCovarianceMatrixSxy(origerr.covarianceMatrixSxy)
        .withCovarianceMatrixSxz(origerr.covarianceMatrixSxz)
        .withCovarianceMatrixSyz(origerr.covarianceMatrixSyz)
        .withCovarianceMatrixStx(origerr.covarianceMatrixStx)
        .withCovarianceMatrixSty(origerr.covarianceMatrixSty)
        .withCovarianceMatrixStz(origerr.covarianceMatrixStz)
        .withStandardErrorOfObservations(origerr.standardErrorOfObservations)
        .withSemiMajorAxisOfError(origerr.semiMajorAxisOfError)
        .withSemiMinorAxisOfError(origerr.semiMinorAxisOfError)
        .withStrikeOfSemiMajorAxis(origerr.strikeOfSemiMajorAxis)
        .withDepthError(origerr.depthError)
        .withOriginTimeError(origerr.originTimeError)
        .withConfidence(origerr.confidence)
        .withCommentId(origerr.commentId)
        .withLoadDate(origerr.loadDate);
    }

    public Builder withOriginId(long originId) {
      this.originId = originId;
      return this;
    }

    public Builder withCovarianceMatrixSxx(double covarianceMatrixSxx) {
      this.covarianceMatrixSxx = covarianceMatrixSxx;
      return this;
    }

    public Builder withCovarianceMatrixSyy(double covarianceMatrixSyy) {
      this.covarianceMatrixSyy = covarianceMatrixSyy;
      return this;
    }

    public Builder withCovarianceMatrixSzz(double covarianceMatrixSzz) {
      this.covarianceMatrixSzz = covarianceMatrixSzz;
      return this;
    }

    public Builder withCovarianceMatrixStt(double covarianceMatrixStt) {
      this.covarianceMatrixStt = covarianceMatrixStt;
      return this;
    }

    public Builder withCovarianceMatrixSxy(double covarianceMatrixSxy) {
      this.covarianceMatrixSxy = covarianceMatrixSxy;
      return this;
    }

    public Builder withCovarianceMatrixSxz(double covarianceMatrixSxz) {
      this.covarianceMatrixSxz = covarianceMatrixSxz;
      return this;
    }

    public Builder withCovarianceMatrixSyz(double covarianceMatrixSyz) {
      this.covarianceMatrixSyz = covarianceMatrixSyz;
      return this;
    }

    public Builder withCovarianceMatrixStx(double covarianceMatrixStx) {
      this.covarianceMatrixStx = covarianceMatrixStx;
      return this;
    }

    public Builder withCovarianceMatrixSty(double covarianceMatrixSty) {
      this.covarianceMatrixSty = covarianceMatrixSty;
      return this;
    }

    public Builder withCovarianceMatrixStz(double covarianceMatrixStz) {
      this.covarianceMatrixStz = covarianceMatrixStz;
      return this;
    }

    public Builder withStandardErrorOfObservations(double standardErrorOfObservations) {
      this.standardErrorOfObservations = standardErrorOfObservations;
      return this;
    }

    public Builder withSemiMajorAxisOfError(double semiMajorAxisOfError) {
      this.semiMajorAxisOfError = semiMajorAxisOfError;
      return this;
    }

    public Builder withSemiMinorAxisOfError(double semiMinorAxisOfError) {
      this.semiMinorAxisOfError = semiMinorAxisOfError;
      return this;
    }

    public Builder withStrikeOfSemiMajorAxis(double strikeOfSemiMajorAxis) {
      this.strikeOfSemiMajorAxis = strikeOfSemiMajorAxis;
      return this;
    }

    public Builder withDepthError(double depthError) {
      this.depthError = depthError;
      return this;
    }

    public Builder withOriginTimeError(double originTimeError) {
      this.originTimeError = originTimeError;
      return this;
    }

    public Builder withConfidence(double confidence) {
      this.confidence = confidence;
      return this;
    }

    public Builder withCommentId(long commentId) {
      this.commentId = commentId;
      return this;
    }

    public Builder withLoadDate(Instant loadDate) {
      this.loadDate = loadDate;
      return this;
    }

    public OrigerrDao build() {

      // NA not allowed
      checkArgument(0 < originId, "Origin Id is " + originId +
        DaoHelperUtility.createGreaterThanString(0));

      // -1.0 indicates NA value
      if (covarianceMatrixSxx != -1.0) {
        checkArgument(0.0 < covarianceMatrixSxx, "covarianceMatrixSxx is " + covarianceMatrixSxx +
          DaoHelperUtility.createGreaterThanString(0));
      }

      // -1.0 indicates NA value
      if (covarianceMatrixSyy != -1.0) {
        checkArgument(0.0 < covarianceMatrixSyy, "covarianceMatrixSyy is " + covarianceMatrixSyy +
          DaoHelperUtility.createGreaterThanString(0));
      }

      // -1.0 indicates NA value
      if (covarianceMatrixSzz != -1.0) {
        checkArgument(0.0 < covarianceMatrixSzz, "covarianceMatrixSzz is " + covarianceMatrixSzz +
          DaoHelperUtility.createGreaterThanString(0));
      }

      // -1.0 indicates NA value
      if (covarianceMatrixStt != -1.0) {
        checkArgument(0.0 < covarianceMatrixStt, "covarianceMatrixStt is " + covarianceMatrixStt +
          DaoHelperUtility.createGreaterThanString(0));
      }

      // -1.0 indicates NA value
      if (standardErrorOfObservations != -1.0) {
        checkArgument(0.0 < standardErrorOfObservations,
          "Standard error of one observation is " + standardErrorOfObservations +
            DaoHelperUtility.createGreaterThanString(0));
      }

      // -1.0 indicates NA value
      if (semiMajorAxisOfError != -1.0) {
        checkArgument(0.0 < semiMajorAxisOfError,
          "Semi-major axis of error ellipse is " + semiMajorAxisOfError +
            DaoHelperUtility.createGreaterThanString(0));
      }

      // -1.0 indicates NA value
      if (semiMinorAxisOfError != -1.0) {
        checkArgument(0.0 < semiMinorAxisOfError,
          "Semi-minor axis of error ellipse is " + semiMinorAxisOfError +
            DaoHelperUtility.createGreaterThanString(0));
      }

      // -1.0 indicates NA value
      if (strikeOfSemiMajorAxis != -1.0) {
        checkArgument((0.0 <= strikeOfSemiMajorAxis) && (strikeOfSemiMajorAxis <= 360.0),
          "Strike of major axis of error ellipse is " + strikeOfSemiMajorAxis +
            DaoHelperUtility.createRangeStringDouble(0, 360, '[', ']'));
      }

      // -1.0 indicates NA value
      if (depthError != -1.0) {
        checkArgument(0.0 < depthError, "Depth error is " + depthError +
          DaoHelperUtility.createGreaterThanString(0));
      }

      // -1.0 indicates NA value
      if (originTimeError != -1.0) {
        checkArgument(0.0 <= originTimeError, "Origin time error is " + originTimeError +
          ".  It must be not be negative.");
      }

      // NA not allowed
      checkArgument((confidence == 0.0) || ((0.5 <= confidence) && (confidence <= 1.0)),
        "Confidence is " + confidence +
          ".  It must be 0.0 or in the range, [0.5, 1.0].");

      // -1 indicates NA value
      if (commentId != -1) {
        checkArgument(0 < commentId, "Comment Id is " + commentId +
          ".  It must not be negative.");
      }

      checkNotNull(loadDate, "Load date is null.");
      // NA not allowed

      return new OrigerrDao(this);
    }

  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OrigerrDao that = (OrigerrDao) o;
    return originId == that.originId
      && Double.compare(that.covarianceMatrixSxx, covarianceMatrixSxx) == 0 &&
      Double.compare(that.covarianceMatrixSyy, covarianceMatrixSyy) == 0
      && Double.compare(that.covarianceMatrixSzz, covarianceMatrixSzz) == 0
      && Double.compare(that.covarianceMatrixStt, covarianceMatrixStt) == 0
      && Double.compare(that.covarianceMatrixSxy, covarianceMatrixSxy) == 0
      && Double.compare(that.covarianceMatrixSxz, covarianceMatrixSxz) == 0
      && Double.compare(that.covarianceMatrixSyz, covarianceMatrixSyz) == 0
      && Double.compare(that.covarianceMatrixStx, covarianceMatrixStx) == 0
      && Double.compare(that.covarianceMatrixSty, covarianceMatrixSty) == 0
      && Double.compare(that.covarianceMatrixStz, covarianceMatrixStz) == 0
      && Double.compare(that.standardErrorOfObservations, standardErrorOfObservations) == 0
      && Double.compare(that.semiMajorAxisOfError, semiMajorAxisOfError) == 0
      && Double.compare(that.semiMinorAxisOfError, semiMinorAxisOfError) == 0
      && Double.compare(that.strikeOfSemiMajorAxis, strikeOfSemiMajorAxis) == 0
      && Double.compare(that.depthError, depthError) == 0
      && Double.compare(that.originTimeError, originTimeError) == 0
      && Double.compare(that.confidence, confidence) == 0 && commentId == that.commentId
      && loadDate.equals(that.loadDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(originId, covarianceMatrixSxx, covarianceMatrixSyy, covarianceMatrixSzz,
      covarianceMatrixStt,
      covarianceMatrixSxy, covarianceMatrixSxz, covarianceMatrixSyz, covarianceMatrixStx,
      covarianceMatrixSty,
      covarianceMatrixStz, standardErrorOfObservations, semiMajorAxisOfError,
      semiMinorAxisOfError, strikeOfSemiMajorAxis,
      depthError, originTimeError, confidence, commentId, loadDate);
  }

  @Override
  public String toString() {
    return "OrigerrDao{" +
      "originId=" + originId +
      ", covarianceMatrixSxx=" + covarianceMatrixSxx +
      ", covarianceMatrixSyy=" + covarianceMatrixSyy +
      ", covarianceMatrixSzz=" + covarianceMatrixSzz +
      ", covarianceMatrixStt=" + covarianceMatrixStt +
      ", covarianceMatrixSxy=" + covarianceMatrixSxy +
      ", covarianceMatrixSxz=" + covarianceMatrixSxz +
      ", covarianceMatrixSyz=" + covarianceMatrixSyz +
      ", covarianceMatrixStx=" + covarianceMatrixStx +
      ", covarianceMatrixSty=" + covarianceMatrixSty +
      ", covarianceMatrixStz=" + covarianceMatrixStz +
      ", standardErrorOfObservations=" + standardErrorOfObservations +
      ", semiMajorAxisOfError=" + semiMajorAxisOfError +
      ", semiMinorAxisOfError=" + semiMinorAxisOfError +
      ", strikeOfSemiMajorAxis=" + strikeOfSemiMajorAxis +
      ", depthError=" + depthError +
      ", originTimeError=" + originTimeError +
      ", confidence=" + confidence +
      ", commentId=" + commentId +
      ", loadDate=" + loadDate +
      '}';
  }
}
