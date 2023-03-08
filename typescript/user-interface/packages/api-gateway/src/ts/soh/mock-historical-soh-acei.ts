import type { ProcessingStationTypes } from '@gms/common-model';
import { SohTypes } from '@gms/common-model';
import {
  getSecureRandomNumber,
  MILLISECONDS_IN_SECOND,
  readJsonData,
  toEpochSeconds
} from '@gms/common-util';
import config from 'config';
import cloneDeep from 'lodash/cloneDeep';
// eslint-disable-next-line import/no-extraneous-dependencies
import path from 'path';

import { gatewayLogger as logger } from '../log/gateway-logger';

/**
 * The input for the query to retrieve decimated historical station SOH where times are Instants.
 */
interface RetrieveDecimatedHistoricalStationSohOsdInput {
  readonly stationName: string;
  readonly startTime: string;
  readonly endTime: string;
  readonly sohMonitorType: SohTypes.SohMonitorType;
  readonly samplesPerChannel: number;
}

/**
 * Input used to query against station trend line (issues) for a specific issue topic where times are Instants
 */
interface UiHistoricalAceiOsdInput {
  stationName: string;
  startTime: string;
  endTime: string;
  type: SohTypes.AceiType;
}

/**
 * Test data paths used when reading in data
 */
interface TestDataPaths {
  readonly dataHome: string;
}

/**
 * Resolves the paths to the test data based of a yaml config
 *
 * @returns Test data paths as TestDataPaths
 */
function resolveTestDataPaths(): TestDataPaths {
  const dataHome = config.get('testData.dataPath');
  logger.debug(`STDS Data:        ${dataHome}`);

  return {
    dataHome
  };
}

/**
 * Encapsulates backend data supporting retrieval by the API gateway.
 */
interface UiStationSohDataStore {
  historicalAceiData: SohTypes.UiHistoricalAcei[];
  stationMap: Map<string, ProcessingStationTypes.ProcessingStation>;
}

// Declare a data store for the data acquisition status mask backend
let dataStore: UiStationSohDataStore;

/**
 * Reads in test data and stores it.
 */
function loadTestData(): UiStationSohDataStore {
  // Get test data configuration settings
  const testDataConfig = config.get('testData');
  const dataPath = resolveTestDataPaths().dataHome;

  // Load historical acei from file
  const historicalAceiPath = path.join(dataPath, testDataConfig.historicalAceiFilename);
  logger.info(`Loading historical acei test data from path: ${historicalAceiPath}`);
  const historicalAceiResponse: any = readJsonData(historicalAceiPath);
  logger.info(`Loaded historical acei number of channels: ${historicalAceiResponse.length} `);

  const stationProcessingConfig = config.get('testData');
  const stationGroupFile = dataPath
    .concat(path.sep)
    .concat(stationProcessingConfig.stationGroupsFileName);
  logger.info(`Loading processing station test data from path: ${stationGroupFile}`);

  // Read the processing network definitions from the configured test set
  let stationGroups: ProcessingStationTypes.ProcessingStationGroup[] = [];
  try {
    stationGroups = readJsonData(stationGroupFile);
    logger.info(`Mock backend processing station loaded ${stationGroups.length} station groups.`);
  } catch (e) {
    logger.error(
      `Failed to read station groups data from files: ` +
        `${stationProcessingConfig.stationGroupsFileName}`
    );
  }

  // Populate the data store maps
  const stationMap: Map<string, ProcessingStationTypes.ProcessingStation> = new Map();
  try {
    stationGroups.forEach(sg => {
      sg.stations.forEach(station => {
        // Add station if not already added (It can be added from a different station group)
        if (!stationMap.has(station.name)) {
          stationMap.set(station.name, station);
        }
      });
    });
  } catch (e) {
    logger.error(`Failed to populate Station and Channel maps using loaded station groups ${e}`);
  }
  logger.debug([...stationMap.keys()]);
  return { historicalAceiData: historicalAceiResponse, stationMap };
}

/**
 * Handle cases where the data store has not been initialized.
 */
function handleUninitializedDataStore() {
  // If the data store is uninitialized, throw an error.
  if (!dataStore) {
    dataStore = loadTestData();
    if (!dataStore) {
      throw new Error('Mock ACEI data store has not been initialized.');
    }
  }
}

/**
 * Retrieve ProcessingStations from the list of station names
 *
 * @param stationNames list of station names
 * @returns a processing station[]
 */
function getStationByName(stationName: string): ProcessingStationTypes.ProcessingStation {
  if (dataStore.stationMap.has(stationName)) {
    return dataStore.stationMap.get(stationName);
  }
  logger.warn(`Failed to find station name ${stationName} in the mock backend datastore!`);

  return undefined;
}

/**
 * Gets historical Acquired Channel Environment Issues (Acei). Useful for unit tests
 *
 * @returns UiHistoricalAcei data
 */
export function getHistoricalAceiData(
  input: UiHistoricalAceiOsdInput
): SohTypes.UiHistoricalAcei[] {
  // Handle undefined input
  if (!input) {
    throw new Error('Unable to retrieve historical ACEI data due to input');
  }

  // Handle undefined input station name
  if (!input.stationName) {
    throw new Error(`Unable to retrieve historical ACEI data due to missing stationName`);
  }

  handleUninitializedDataStore();
  // Grab first in the json list and use it as a template
  const templateAcei: SohTypes.UiHistoricalAcei = dataStore.historicalAceiData[0];
  const station: ProcessingStationTypes.ProcessingStation = getStationByName(input.stationName);

  // Handle undefined station
  if (!station || !station.channels) {
    throw new Error(
      `Unable to retrieve historical ACEI data station ${input.stationName} not found`
    );
  }
  // Walk thru the processing station's channels and create a new entry for each.
  const aceiResult: SohTypes.UiHistoricalAcei[] = station.channels.map(channel => {
    const startTime: number = toEpochSeconds(input.startTime) * 1000;
    const endTime: number = toEpochSeconds(input.endTime) * 1000;

    const entry: SohTypes.UiHistoricalAcei = cloneDeep(templateAcei);
    entry.channelName = channel.name;
    entry.monitorType = input.type;
    // override the template data because it uses the wrong times and isn't very helpful
    const size = Math.floor(getSecureRandomNumber() * 1000);
    const stepSize = (endTime - startTime) / size;
    const steps = new Array(size).fill(null).map((v, index) => startTime + stepSize * index);

    entry.issues = [[...steps.map((s, index) => [s, index % 2 === 0 ? 0 : 1])]];
    return entry;
  });

  logger.info(
    `Soh Mock backend returning historical ACEI number of channels: ${aceiResult.length}`
  );
  return aceiResult;
}

/**
 * Calculates the Historical SOH trend data for when running the UI locally
 *
 * @param input RetrieveDecimatedHistoricalStationSohOsdInput OSD version where times are Instant strings
 * @returns dummy generated UiHistoricalSoh
 */
export function getHistoricalSohData(
  input: RetrieveDecimatedHistoricalStationSohOsdInput
): SohTypes.UiHistoricalSoh {
  // Handle undefined input
  if (!input) {
    throw new Error('Unable to retrieve historical SOH data due to input');
  }

  // Handle undefined input station name
  if (!input.stationName) {
    throw new Error(`Unable to retrieve historical SOH data due to missing stationName`);
  }
  handleUninitializedDataStore();

  const startTimeGeneration = Date.now();

  // Convert strings to epoch seconds
  const startTimeMs: number = toEpochSeconds(input.startTime) * 1000;
  const endTimeMs: number = toEpochSeconds(input.endTime) * 1000;

  // randomly generate values for the provided range; where there is a value every 10 seconds
  const range = Math.round(Math.abs(endTimeMs - startTimeMs) / MILLISECONDS_IN_SECOND);

  // default send one data point every 10 seconds; however adjust based on the samplesPerChannel setting
  const defaultStepSize = 10;
  const stepSize = Math.round(
    range / input.samplesPerChannel > defaultStepSize
      ? range / input.samplesPerChannel
      : defaultStepSize
  );

  const defaultNumberOfValuesPerChannel = Math.round(range / defaultStepSize);
  const numberOfValuesPerChannel = Math.round(range / stepSize);

  const type =
    input.sohMonitorType === SohTypes.SohMonitorType.MISSING
      ? SohTypes.SohValueType.PERCENT
      : SohTypes.SohValueType.DURATION;
  const station: ProcessingStationTypes.ProcessingStation = getStationByName(input.stationName);
  // Handle undefined station
  if (!station || !station.channels) {
    throw new Error(
      `Unable to retrieve historical SOH data station ${input.stationName} not found`
    );
  }
  const monitorValues: SohTypes.MonitorValue[] = station.channels.map(channel => {
    const padding =
      // eslint-disable-next-line @typescript-eslint/no-magic-numbers
      (input.sohMonitorType === SohTypes.SohMonitorType.MISSING ? 100 : 1000) *
      getSecureRandomNumber();
    const values: number[] = new Array(numberOfValuesPerChannel)
      .fill(1)
      .map(() => getSecureRandomNumber() * padding);

    return {
      channelName: channel.name,
      values: {
        values,
        type
      },
      average: values && values.length > 0 ? values.reduce((a, b) => a + b) / values.length : 0
    };
  });

  logger.info(
    `Soh Mock backend returning historical SOH number of channels: ${monitorValues.length}`
  );

  // provide calculations times that match the query start and end time
  const calculationTimes = new Array(numberOfValuesPerChannel)
    .fill(null)
    .map((v, index) => startTimeMs + stepSize * MILLISECONDS_IN_SECOND * index);
  calculationTimes[0] = startTimeMs;
  calculationTimes[calculationTimes.length - 1] = endTimeMs;

  const generationTimeSpentSeconds = (Date.now() - startTimeGeneration) / 1000;
  logger.info(
    `Generated historical SOH data startTime:${startTimeMs} endTime:${endTimeMs} stationName:${
      input.stationName
    } monitorType:${input.sohMonitorType} range:${range} stepSize:${stepSize} numberOfChannels:${
      station.channels.length
    } numberOfValuesPerChannel:${numberOfValuesPerChannel} totalValues:${
      station.channels.length * numberOfValuesPerChannel
    } timeInSeconds:${generationTimeSpentSeconds}`
  );

  return {
    stationName: input.stationName,
    calculationTimes,
    monitorValues,
    // eslint-disable-next-line @typescript-eslint/no-magic-numbers
    percentageSent: (numberOfValuesPerChannel / defaultNumberOfValuesPerChannel) * 100
  };
}
