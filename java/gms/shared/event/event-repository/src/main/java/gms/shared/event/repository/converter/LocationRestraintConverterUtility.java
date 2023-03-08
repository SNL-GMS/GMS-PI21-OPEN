package gms.shared.event.repository.converter;

import gms.shared.event.coi.DepthRestraintReason;
import gms.shared.event.coi.LocationRestraint;
import gms.shared.event.coi.LocationSolution;
import gms.shared.event.coi.RestraintType;
import gms.shared.event.coi.type.DepthMethod;
import gms.shared.event.dao.EventControlDao;
import gms.shared.event.dao.OriginDao;
import gms.shared.event.repository.BridgedEhInformation;
import gms.shared.event.repository.BridgedSdhInformation;
import gms.shared.signaldetection.dao.css.AssocDao;
import gms.shared.signaldetection.dao.css.enums.DefiningFlag;
import gms.shared.utilities.bridge.database.converter.InstantToDoubleConverterNegativeNa;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;

/**
 * Utility class to help create {@link LocationRestraint} objects
 */
public class LocationRestraintConverterUtility {

  private static final InstantToDoubleConverterNegativeNa instantToDoubleConverter = new InstantToDoubleConverterNegativeNa();
  private static final Set<String> PREFERRED_LOCATION_SET = Set.of("F", "S", "R");

  //private constructor for sonarqube
  private LocationRestraintConverterUtility() {
  }

  /**
   * Returns the {@link LocationRestraint} object for use in creating a {@link LocationSolution}
   *
   * @param ehInfo Bridged EventHypothesis Information
   * @param sdhInfo Bridged SignalDetectionHypothesis Information
   * @return The newly created {@link LocationRestraint} COI object
   */
  public static LocationRestraint fromLegacyToLocationRestraint(BridgedEhInformation ehInfo,
    Collection<BridgedSdhInformation> sdhInfo) {
    checkNotNull(ehInfo, "BridgedEhInformation cannot be null");
    checkNotNull(sdhInfo, "BridgedSdhInformation cannot be null");
    checkArgument(!sdhInfo.isEmpty(), "Must provide at least one BridgedSdhInformation");


    var originDao = ehInfo.getOriginDao();
    var eventControlDao = ehInfo.getEventControlDao();
    var assocDaos = sdhInfo.stream()
      .map(BridgedSdhInformation::getAssocDao)
      .collect(toSet());

    return eventControlDao.map(ecDao -> buildRestraintWithEventControl(originDao, ecDao, assocDaos))
      .orElseGet(() -> buildRestraintFromOrigin(originDao, assocDaos));
  }

  private static Optional<DepthRestraintReason> handleRestraintType(OriginDao originDao,
    Set<AssocDao> assocDaos, RestraintType depthRestraint) {
    var depthRestraintReason = Optional.<DepthRestraintReason>empty();
    if (RestraintType.FIXED.equals(depthRestraint)) {
      depthRestraintReason = Optional.of(DepthRestraintReason.OTHER);
      if (originDao.getDepth() == 0.0) {
        depthRestraintReason = Optional.of(DepthRestraintReason.FIXED_AT_SURFACE);
      } else if (DepthMethod.A.equals(originDao.getDepthMethod())) {
        depthRestraintReason = Optional.of(DepthRestraintReason.FIXED_AT_STANDARD_DEPTH_FOR_ECM);
      } else if (DepthMethod.G.equals(originDao.getDepthMethod())) {
        if (!assocDaos.stream()
          .filter(assoc -> (assoc.getId().getOriginId() == originDao.getOriginId()))
          .filter(assoc -> ("pP".equals(assoc.getPhase()) || "sP".equals(assoc.getPhase())))
          .filter(assoc -> (!DefiningFlag.isDefining(assoc.getTimeDefining()) &&
            !DefiningFlag.isDefining(assoc.getAzimuthDefining()) &&
            !DefiningFlag.isDefining(assoc.getSlownessDefining())))
          .collect(toSet()).isEmpty()) {
          depthRestraintReason = Optional.of(
            DepthRestraintReason.FIXED_AT_DEPTH_FOUND_USING_DEPTH_PHASE_MEASUREMENTS);
        } else {
          depthRestraintReason = Optional.of(DepthRestraintReason.FIXED_BY_ANALYST);
        }
      }
    }
    return depthRestraintReason;
  }

  private static LocationRestraint buildRestraintWithEventControl(OriginDao originDao, EventControlDao eventControlDao,
    Set<AssocDao> assocDaos) {
    checkArgument(PREFERRED_LOCATION_SET.contains(eventControlDao.getPreferredLocation()),
      format("Invalid EventControl PreferredLocation for LocationRestraint. Expected:[F, S, R] Actual:[%s]",
        eventControlDao.getPreferredLocation()));

    final LocationRestraint locationRestraint;
    if ("F".equals(eventControlDao.getPreferredLocation())) {
      locationRestraint = LocationRestraint.free();
    } else if ("S".equals(eventControlDao.getPreferredLocation())) {
      locationRestraint = LocationRestraint.surface();
    } else {
      var depthRestraint = RestraintType.UNRESTRAINED;
      var depthRestraintKm = Optional.<Double>empty();
      var positionRestraint = RestraintType.UNRESTRAINED;
      var latRestraintDeg = Optional.<Double>empty();
      var longRestraintDeg = Optional.<Double>empty();
      var timeRestraintType = RestraintType.UNRESTRAINED;
      var timeRestraint = Optional.<Instant>empty();

      if (eventControlDao.getConstrainDepth()) {
        depthRestraint = RestraintType.FIXED;
        depthRestraintKm = Optional.of(originDao.getDepth());
      }

      if (eventControlDao.getConstrainLatLon()) {
        positionRestraint = RestraintType.FIXED;
        latRestraintDeg = Optional.of(originDao.getLatitude());
        longRestraintDeg = Optional.of(originDao.getLongitude());
      }

      if (eventControlDao.getConstrainOriginTime()) {
        timeRestraintType = RestraintType.FIXED;
        timeRestraint = Optional.of(
          instantToDoubleConverter.convertToEntityAttribute(originDao.getEpoch()));
      }

      var depthRestraintReason = handleRestraintType(originDao, assocDaos, depthRestraint);

      var lrBuilder = LocationRestraint.builder()
        .setDepthRestraintType(depthRestraint)
        .setDepthRestraintKm(depthRestraintKm)
        .setPositionRestraintType(positionRestraint)
        .setLatitudeRestraintDegrees(latRestraintDeg)
        .setLongitudeRestraintDegrees(longRestraintDeg)
        .setTimeRestraintType(timeRestraintType)
        .setTimeRestraint(timeRestraint);
      if (depthRestraintReason.isPresent()) {
        lrBuilder.setDepthRestraintReason(depthRestraintReason);
      }
      locationRestraint = lrBuilder.build();
    }
    return locationRestraint;
  }

  private static LocationRestraint buildRestraintFromOrigin(OriginDao originDao, Set<AssocDao> assocDaos) {

    checkNotNull(originDao, "originDao cannot be null");
    checkNotNull(assocDaos, "assocDaos cannot be null");

    final LocationRestraint locationRestraint;

    var depthRestraint = RestraintType.UNRESTRAINED;
    var depthRestraintReason = Optional.<DepthRestraintReason>empty();
    var depthRestraintKm = Optional.<Double>empty();
    final var positionRestraint = RestraintType.UNRESTRAINED;
    final var latRestraintDeg = Optional.<Double>empty();
    final var longRestraintDeg = Optional.<Double>empty();
    final var timeRestraintType = RestraintType.UNRESTRAINED;
    final var timeRestraint = Optional.<Instant>empty();

    switch (originDao.getDepthMethod()) {
      case A:
      case R:
      case G:
        depthRestraint = RestraintType.FIXED;
        depthRestraintKm = Optional.of(originDao.getDepth());
        break;
      default:
        break;
    }

    switch (originDao.getDepthMethod()) {
      case A:
        depthRestraintReason = Optional.of(DepthRestraintReason.FIXED_AT_STANDARD_DEPTH_FOR_ECM);
        break;
      case R:
        depthRestraintReason = originDao.getDepth() == 0 ? Optional.of(DepthRestraintReason.FIXED_AT_SURFACE) : Optional.of(DepthRestraintReason.OTHER);
        break;
      case G:
        if (!assocDaos.stream()
          .filter(assoc -> (assoc.getId().getOriginId() == originDao.getOriginId()))
          .filter(assoc -> ("pP".equals(assoc.getPhase()) || "sP".equals(assoc.getPhase())))
          .filter(assoc -> (!DefiningFlag.isDefining(assoc.getTimeDefining()) &&
            !DefiningFlag.isDefining(assoc.getAzimuthDefining()) &&
            !DefiningFlag.isDefining(assoc.getSlownessDefining())))
          .collect(toSet()).isEmpty()) {
          depthRestraintReason = Optional.of(
            DepthRestraintReason.FIXED_AT_DEPTH_FOUND_USING_DEPTH_PHASE_MEASUREMENTS);
        } else {
          depthRestraintReason = Optional.of(DepthRestraintReason.FIXED_BY_ANALYST);
        }
        break;
      default:
        break;
    }

    var lrBuilder = LocationRestraint.builder()
      .setDepthRestraintType(depthRestraint)
      .setDepthRestraintKm(depthRestraintKm)
      .setPositionRestraintType(positionRestraint)
      .setLatitudeRestraintDegrees(latRestraintDeg)
      .setLongitudeRestraintDegrees(longRestraintDeg)
      .setTimeRestraintType(timeRestraintType)
      .setTimeRestraint(timeRestraint);
    if (depthRestraintReason.isPresent()) {
      lrBuilder.setDepthRestraintReason(depthRestraintReason);
    }

    locationRestraint = lrBuilder.build();
    return locationRestraint;
  }
}
