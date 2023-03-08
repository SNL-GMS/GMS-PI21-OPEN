package gms.shared.stationdefinition.factory;

import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.stationdefinition.accessor.BridgedStationDefinitionAccessor;
import gms.shared.stationdefinition.api.StationDefinitionAccessorInterface;
import gms.shared.stationdefinition.cache.VersionCache;
import gms.shared.stationdefinition.converter.DaoCalibrationConverter;
import gms.shared.stationdefinition.converter.DaoChannelConverter;
import gms.shared.stationdefinition.converter.DaoChannelGroupConverter;
import gms.shared.stationdefinition.converter.DaoResponseConverter;
import gms.shared.stationdefinition.converter.DaoStationConverter;
import gms.shared.stationdefinition.converter.DaoStationGroupConverter;
import gms.shared.stationdefinition.converter.FileFrequencyAmplitudePhaseConverter;
import gms.shared.stationdefinition.converter.util.assemblers.ChannelAssembler;
import gms.shared.stationdefinition.converter.util.assemblers.ChannelGroupAssembler;
import gms.shared.stationdefinition.converter.util.assemblers.ResponseAssembler;
import gms.shared.stationdefinition.converter.util.assemblers.StationAssembler;
import gms.shared.stationdefinition.converter.util.assemblers.StationGroupAssembler;
import gms.shared.stationdefinition.database.connector.SiteDatabaseConnector;
import gms.shared.stationdefinition.database.connector.factory.StationDefinitionDatabaseConnectorFactory;
import gms.shared.stationdefinition.repository.BridgedChannelGroupRepository;
import gms.shared.stationdefinition.repository.BridgedChannelRepository;
import gms.shared.stationdefinition.repository.BridgedResponseRepository;
import gms.shared.stationdefinition.repository.BridgedStationGroupRepository;
import gms.shared.stationdefinition.repository.BridgedStationRepository;
import gms.shared.stationdefinition.repository.util.StationDefinitionIdUtility;
import org.apache.commons.lang3.Validate;

@Deprecated
public class StationDefinitionAccessorFactory {
  static final String OPERATIONAL_TIME_PERIOD_CONFIG = "global.operational-time-period";
  static final String OPERATIONAL_PERIOD_START = "operationalPeriodStart";
  static final String OPERATIONAL_PERIOD_END = "operationalPeriodEnd";

  static final String STAT_DEF_COMPONENT_NAME = "station-definition";
  private static volatile StationDefinitionAccessorFactory instance;
  private final StationDefinitionDatabaseConnectorFactory stationDefinitionDatabaseConnectorFactory;
  private DaoStationGroupConverter daoStationGroupConverterInstance;
  private DaoStationConverter daoStationConverterInstance;
  private DaoChannelGroupConverter daoChannelGroupConverterInstance;
  private DaoChannelConverter daoChannelConverterInstance;
  private DaoResponseConverter daoResponseConverterInstance;
  private DaoCalibrationConverter daoCalibrationConverterInstance;
  private FileFrequencyAmplitudePhaseConverter fileFrequencyAmplitudePhaseConverterInstance;
  private StationGroupAssembler stationGroupAssemblerInstance;
  private StationAssembler stationAssemblerInstance;
  private ChannelGroupAssembler channelGroupAssemblerInstance;
  private ChannelAssembler channelAssemblerInstance;

  private BridgedStationGroupRepository bridgedStationGroupRepositoryInstance;
  private BridgedStationRepository bridgedStationRepositoryInstance;
  private BridgedChannelGroupRepository bridgedChannelGroupRepositoryInstance;
  private BridgedChannelRepository bridgedChannelRepositoryInstance;
  private BridgedResponseRepository bridgedResponseRepositoryInstance;
  private ResponseAssembler responseAssemblerInstance;
  private BridgedStationDefinitionAccessor bridgedStationDefinitionAccessor;

  private StationDefinitionAccessorFactory(
    StationDefinitionDatabaseConnectorFactory stationDefinitionDatabaseConnectorFactory) {
    this.stationDefinitionDatabaseConnectorFactory = stationDefinitionDatabaseConnectorFactory;
  }

  /**
   * Creates an IOC factory to build the station definition managers and their dependencies
   *
   * @param stationDefinitionDatabaseConnectorFactory an IOC factory that builds the station definition jpa repositories
   * used by the dependencies of the station definition managers
   * @return station group cache instance
   */
  @Deprecated
  public static StationDefinitionAccessorFactory create(
    StationDefinitionDatabaseConnectorFactory stationDefinitionDatabaseConnectorFactory) {
    Validate.notNull(stationDefinitionDatabaseConnectorFactory,
      "A StationDefinitionJpaRepositoryFactory must be provided.");
    if (instance == null) {
      synchronized (StationDefinitionAccessorFactory.class) {
        if (instance == null) {
          instance = new StationDefinitionAccessorFactory(stationDefinitionDatabaseConnectorFactory);
        }
      }
    }

    return instance;
  }

  private DaoStationGroupConverter getDaoStationGroupConverterInstance() {
    if (daoStationGroupConverterInstance == null) {
      daoStationGroupConverterInstance = new DaoStationGroupConverter();
    }
    return daoStationGroupConverterInstance;
  }

  private DaoStationConverter getDaoStationConverterInstance() {
    if (daoStationConverterInstance == null) {
      daoStationConverterInstance = new DaoStationConverter();
    }
    return daoStationConverterInstance;
  }

  private DaoChannelGroupConverter getDaoChannelGroupConverterInstance() {

    if (daoChannelGroupConverterInstance == null) {
      daoChannelGroupConverterInstance = new DaoChannelGroupConverter();
    }
    return daoChannelGroupConverterInstance;
  }

  private DaoChannelConverter getDaoChannelConverterInstance() {
    if (daoChannelConverterInstance == null) {
      daoChannelConverterInstance = new DaoChannelConverter(
        getDaoCalibrationConverterInstance(),
        getFileFrequencyAmplitudePhaseConverterInstance());
    }
    return daoChannelConverterInstance;
  }

  private DaoResponseConverter getDaoResponseConverterInstance() {

    if (daoResponseConverterInstance == null) {
      daoResponseConverterInstance = new DaoResponseConverter();
    }
    return daoResponseConverterInstance;
  }

  private DaoCalibrationConverter getDaoCalibrationConverterInstance() {

    if (daoCalibrationConverterInstance == null) {
      daoCalibrationConverterInstance = new DaoCalibrationConverter();
    }
    return daoCalibrationConverterInstance;
  }

  private FileFrequencyAmplitudePhaseConverter getFileFrequencyAmplitudePhaseConverterInstance() {

    if (fileFrequencyAmplitudePhaseConverterInstance == null) {
      fileFrequencyAmplitudePhaseConverterInstance = new FileFrequencyAmplitudePhaseConverter();
    }
    return fileFrequencyAmplitudePhaseConverterInstance;
  }

  private StationGroupAssembler getStationGroupAssemblerInstance() {
    if (stationGroupAssemblerInstance == null) {
      stationGroupAssemblerInstance = new StationGroupAssembler(
        getDaoStationGroupConverterInstance());
    }
    return stationGroupAssemblerInstance;
  }

  private StationAssembler getStationAssemblerInstance() {
    if (stationAssemblerInstance == null) {
      stationAssemblerInstance = new StationAssembler(
        getDaoStationConverterInstance());
    }
    return stationAssemblerInstance;
  }

  private ChannelGroupAssembler getChannelGroupAssemblerInstance() {

    if (channelGroupAssemblerInstance == null) {
      channelGroupAssemblerInstance = new ChannelGroupAssembler(
        getDaoChannelGroupConverterInstance());
    }
    return channelGroupAssemblerInstance;
  }

  private ChannelAssembler getChannelAssemblerInstance() {
    if (channelAssemblerInstance == null) {
      channelAssemblerInstance = new ChannelAssembler(
        getDaoChannelConverterInstance(), getDaoResponseConverterInstance());
    }
    return channelAssemblerInstance;
  }

  private ResponseAssembler getResponseAssemblerInstance() {
    if (responseAssemblerInstance == null) {
      responseAssemblerInstance = new ResponseAssembler(getDaoResponseConverterInstance(),
        getDaoCalibrationConverterInstance(),
        getFileFrequencyAmplitudePhaseConverterInstance());
    }

    return responseAssemblerInstance;
  }

  private BridgedStationGroupRepository getBridgedStationGroupRepositoryInstance() {
    if (bridgedStationGroupRepositoryInstance == null) {
      bridgedStationGroupRepositoryInstance = new BridgedStationGroupRepository(
        getBridgedStationRepositoryInstance(),
        stationDefinitionDatabaseConnectorFactory.getNetworkDatabaseConnectorInstance(),
        stationDefinitionDatabaseConnectorFactory.getAffiliationDatabaseConnectorInstance(),
        stationDefinitionDatabaseConnectorFactory.getSiteDatabaseConnectorInstance(),
        getStationGroupAssemblerInstance());
    }
    return bridgedStationGroupRepositoryInstance;
  }

  private BridgedStationRepository getBridgedStationRepositoryInstance() {
    if (bridgedStationRepositoryInstance == null) {
      bridgedStationRepositoryInstance = new BridgedStationRepository(
        getBridgedChannelRepositoryInstance(),
        getBridgedChannelGroupRepositoryInstance(),
        stationDefinitionDatabaseConnectorFactory.getSiteDatabaseConnectorInstance(),
        stationDefinitionDatabaseConnectorFactory.getSiteChanDatabaseConnectorInstance(),
        getStationAssemblerInstance());
    }
    return bridgedStationRepositoryInstance;
  }

  @Deprecated
  public BridgedChannelGroupRepository getBridgedChannelGroupRepositoryInstance() {
    if (bridgedChannelGroupRepositoryInstance == null) {
      bridgedChannelGroupRepositoryInstance = new BridgedChannelGroupRepository(
        stationDefinitionDatabaseConnectorFactory.getSiteDatabaseConnectorInstance(),
        stationDefinitionDatabaseConnectorFactory.getSiteChanDatabaseConnectorInstance(),
        getChannelGroupAssemblerInstance(),
        getBridgedChannelRepositoryInstance());
    }
    return bridgedChannelGroupRepositoryInstance;
  }

  @Deprecated
  public BridgedChannelRepository getBridgedChannelRepositoryInstance() {
    if (bridgedChannelRepositoryInstance == null) {
      var systemConfig = SystemConfig.create(STAT_DEF_COMPONENT_NAME);
      bridgedChannelRepositoryInstance = new BridgedChannelRepository(
        stationDefinitionDatabaseConnectorFactory.getBeamDatabaseConnectorInstance(),
        stationDefinitionDatabaseConnectorFactory.getSiteDatabaseConnectorInstance(),
        stationDefinitionDatabaseConnectorFactory.getSiteChanDatabaseConnectorInstance(),
        stationDefinitionDatabaseConnectorFactory.getSensorDatabaseConnectorInstance(),
        stationDefinitionDatabaseConnectorFactory.getWfdiscDatabaseConnectorInstance(),
        getChannelAssemblerInstance(),
        new StationDefinitionIdUtility(systemConfig),
        new VersionCache(systemConfig),
        getBridgedResponseRepositoryInstance());
    }
    return bridgedChannelRepositoryInstance;
  }

  private BridgedResponseRepository getBridgedResponseRepositoryInstance() {
    if (bridgedResponseRepositoryInstance == null) {
      bridgedResponseRepositoryInstance = new BridgedResponseRepository(
        stationDefinitionDatabaseConnectorFactory.getWfdiscDatabaseConnectorInstance(),
        stationDefinitionDatabaseConnectorFactory.getSensorDatabaseConnectorInstance(),
        stationDefinitionDatabaseConnectorFactory.getInstrumentDatabaseConnectorInstance(),
        new StationDefinitionIdUtility(SystemConfig.create(STAT_DEF_COMPONENT_NAME)),
        getResponseAssemblerInstance());
    }

    return bridgedResponseRepositoryInstance;
  }

  @Deprecated
  public StationDefinitionAccessorInterface getBridgedStationDefinitionAccessorInstance() {
    if (bridgedStationDefinitionAccessor == null) {
      bridgedStationDefinitionAccessor = new BridgedStationDefinitionAccessor(
        SystemConfig.create(STAT_DEF_COMPONENT_NAME),
        getBridgedStationGroupRepositoryInstance(),
        getBridgedStationRepositoryInstance(),
        getBridgedChannelGroupRepositoryInstance(),
        getBridgedChannelRepositoryInstance(),
        getBridgedResponseRepositoryInstance());
    }
    return bridgedStationDefinitionAccessor;
  }

  @Deprecated
  public SiteDatabaseConnector getSiteDatabaseConnector() {
    return stationDefinitionDatabaseConnectorFactory.getSiteDatabaseConnectorInstance();
  }
}
