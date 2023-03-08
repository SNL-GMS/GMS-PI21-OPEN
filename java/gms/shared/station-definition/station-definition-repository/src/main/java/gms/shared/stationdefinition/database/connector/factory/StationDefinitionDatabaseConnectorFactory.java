package gms.shared.stationdefinition.database.connector.factory;

import gms.shared.stationdefinition.database.connector.AffiliationDatabaseConnector;
import gms.shared.stationdefinition.database.connector.BeamDatabaseConnector;
import gms.shared.stationdefinition.database.connector.InstrumentDatabaseConnector;
import gms.shared.stationdefinition.database.connector.NetworkDatabaseConnector;
import gms.shared.stationdefinition.database.connector.SensorDatabaseConnector;
import gms.shared.stationdefinition.database.connector.SiteChanDatabaseConnector;
import gms.shared.stationdefinition.database.connector.SiteDatabaseConnector;
import gms.shared.stationdefinition.database.connector.WfdiscDatabaseConnector;
import gms.shared.stationdefinition.database.connector.WftagDatabaseConnector;
import org.apache.commons.lang3.Validate;

import javax.persistence.EntityManagerFactory;

@Deprecated
public class StationDefinitionDatabaseConnectorFactory {

  private NetworkDatabaseConnector networkDatabaseConnectorInstance;
  private AffiliationDatabaseConnector affiliationDatabaseConnectorInstance;
  private SiteDatabaseConnector siteDatabaseConnectorInstance;
  private SiteChanDatabaseConnector siteChanDatabaseConnectorInstance;
  private SensorDatabaseConnector sensorDatabaseConnectorInstance;
  private InstrumentDatabaseConnector instrumentDatabaseConnectorInstance;
  private WfdiscDatabaseConnector wfdiscDatabaseConnectorInstance;
  private WftagDatabaseConnector wftagDatabaseConnectorInstance;
  private BeamDatabaseConnector beamDatabaseConnectorInstance;

  private final EntityManagerFactory stationDefinitionEntityManagerFactory;

  private StationDefinitionDatabaseConnectorFactory(EntityManagerFactory entityManagerFactory) {
    stationDefinitionEntityManagerFactory = entityManagerFactory;
  }

  /**
   * Creates an IOC factory to build the station definition jpa repositories
   *
   * @param entityManagerFactory the entity manager that supplies the connections to be used by the db connectors
   * @return stationDefinitionDatabaseConnectorFactory object
   */
  @Deprecated
  public static StationDefinitionDatabaseConnectorFactory create(
    EntityManagerFactory entityManagerFactory) {
    Validate.notNull(entityManagerFactory, "An EntityManagerFactory must be provided.");
    return new StationDefinitionDatabaseConnectorFactory(entityManagerFactory);
  }

  @Deprecated
  public NetworkDatabaseConnector getNetworkDatabaseConnectorInstance() {
    if (networkDatabaseConnectorInstance == null) {
      networkDatabaseConnectorInstance = new NetworkDatabaseConnector(stationDefinitionEntityManagerFactory);
    }
    return networkDatabaseConnectorInstance;
  }

  @Deprecated
  public AffiliationDatabaseConnector getAffiliationDatabaseConnectorInstance() {
    if (affiliationDatabaseConnectorInstance == null) {
      affiliationDatabaseConnectorInstance = new AffiliationDatabaseConnector(stationDefinitionEntityManagerFactory);
    }
    return affiliationDatabaseConnectorInstance;
  }

  @Deprecated
  public SiteDatabaseConnector getSiteDatabaseConnectorInstance() {
    if (siteDatabaseConnectorInstance == null) {
      siteDatabaseConnectorInstance = new SiteDatabaseConnector(stationDefinitionEntityManagerFactory);
    }
    return siteDatabaseConnectorInstance;
  }

  @Deprecated
  public SiteChanDatabaseConnector getSiteChanDatabaseConnectorInstance() {
    if (siteChanDatabaseConnectorInstance == null) {
      siteChanDatabaseConnectorInstance = new SiteChanDatabaseConnector(stationDefinitionEntityManagerFactory);
    }
    return siteChanDatabaseConnectorInstance;
  }

  @Deprecated
  public InstrumentDatabaseConnector getInstrumentDatabaseConnectorInstance() {
    if (instrumentDatabaseConnectorInstance == null) {
      instrumentDatabaseConnectorInstance = new InstrumentDatabaseConnector(stationDefinitionEntityManagerFactory);
    }
    return instrumentDatabaseConnectorInstance;
  }

  @Deprecated
  public SensorDatabaseConnector getSensorDatabaseConnectorInstance() {
    if (sensorDatabaseConnectorInstance == null) {
      sensorDatabaseConnectorInstance = new SensorDatabaseConnector(stationDefinitionEntityManagerFactory);
    }
    return sensorDatabaseConnectorInstance;
  }

  @Deprecated
  public WfdiscDatabaseConnector getWfdiscDatabaseConnectorInstance() {
    if (wfdiscDatabaseConnectorInstance == null) {
      wfdiscDatabaseConnectorInstance = new WfdiscDatabaseConnector(stationDefinitionEntityManagerFactory);
    }
    return wfdiscDatabaseConnectorInstance;
  }

  /**
   * Get singleton WftagDatabaseConnector Instance
   *
   * @return WftagDatabaseConnector
   */
  @Deprecated
  public WftagDatabaseConnector getWftagfDatabaseConnectorInstance() {
    if (wftagDatabaseConnectorInstance == null) {
      wftagDatabaseConnectorInstance = new WftagDatabaseConnector(stationDefinitionEntityManagerFactory);
    }
    return wftagDatabaseConnectorInstance;
  }

  /**
   * Get singleton BeamDatabaseConnector Instance
   *
   * @return BeamDatabaseConnector
   */
  @Deprecated
  public BeamDatabaseConnector getBeamDatabaseConnectorInstance() {
    if (beamDatabaseConnectorInstance == null) {
      beamDatabaseConnectorInstance = new BeamDatabaseConnector(stationDefinitionEntityManagerFactory);
    }
    return beamDatabaseConnectorInstance;
  }

}
