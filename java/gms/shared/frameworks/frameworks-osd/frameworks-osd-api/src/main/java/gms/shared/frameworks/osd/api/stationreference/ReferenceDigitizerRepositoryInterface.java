package gms.shared.frameworks.osd.api.stationreference;

import gms.shared.frameworks.osd.coi.stationreference.ReferenceDigitizer;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceDigitizerMembership;

import java.util.List;
import java.util.UUID;

public interface ReferenceDigitizerRepositoryInterface {

  /**
   * Gets all digitizers
   *
   * @return all digitizers
   */
  List<ReferenceDigitizer> retrieveDigitizers();

  /**
   * Retrieve all digitizers by entity id.
   *
   * @param id the id
   * @return all digitizer versions with that entity id
   */
  List<ReferenceDigitizer> retrieveDigitizersByEntityId(UUID id);

  /**
   * Retrieve all digitizers by name.
   *
   * @param name the name
   * @return all digitizer versions with that name
   */
  List<ReferenceDigitizer> retrieveDigitizersByName(String name);

  /**
   * Store ReferenceDigitizer to the relational database.
   *
   * @param digitizer the digitizer
   */
  void storeReferenceDigitizer(ReferenceDigitizer digitizer);


  /**
   * Retrieves all digitizer memberships
   *
   * @return the memberships
   */
  List<ReferenceDigitizerMembership> retrieveDigitizerMemberships();

  /**
   * Retrieves digitizer memberships with the given digitizer entity id.
   *
   * @return the memberships
   */
  List<ReferenceDigitizerMembership> retrieveDigitizerMembershipsByDigitizerId(UUID id);

  /**
   * Retrieves digitizer memberships with the given channel entity id.
   *
   * @return the memberships
   */
  List<ReferenceDigitizerMembership> retrieveDigitizerMembershipsByChannelId(UUID id);

  /**
   * Retrieves digitizer memberships with the given digitizer entity id and channel entity id.
   *
   * @return the memberships
   */
  List<ReferenceDigitizerMembership> retrieveDigitizerMembershipsByDigitizerAndChannelId(
    UUID digitizerId, UUID channelId);

  /**
   * Store a digitizer membership to the database.
   *
   * @param membership the object to store
   */
  void storeDigitizerMembership(ReferenceDigitizerMembership membership);

}
