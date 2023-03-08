import {
  CommonTypes,
  ConfigurationTypes,
  Displays,
  LegacyEventTypes,
  SohTypes,
  WorkflowTypes
} from '@gms/common-model';
import { Logger } from '@gms/common-util';
import type { AppState, UseQueryStateResult } from '@gms/ui-state';
import {
  AnalystWorkspaceTypes,
  DEFAULT_INITIAL_WAVEFORM_LOADING_STATE,
  FilterableSOHTypes
} from '@gms/ui-state';
import { signalDetectionsData } from '@gms/ui-state/__tests__/__data__';
import { AlignWaveformsOn } from '@gms/ui-state/lib/app/state/analyst/types';
import map from 'lodash/map';
import uniq from 'lodash/uniq';
import uniqBy from 'lodash/uniqBy';

import type { AnalystCurrentFk } from '../../src/ts/components/analyst-ui/components/azimuth-slowness/components/fk-rendering/fk-rendering';
import type { AzimuthSlownessReduxProps } from '../../src/ts/components/analyst-ui/components/azimuth-slowness/types';
import { eventData } from './event-data';

const logger = Logger.create('GMS_LOG_JEST', process.env.GMS_LOG_JEST);

// 11:59:59 05/19/2010
export const startTimeSeconds = 1274313599;

// 02:00:01 05/20/2010
export const endTimeSeconds = 1274320801;

// time block 2 hours = 7200 seconds
export const timeBlock = 7200;

export const timeInterval: CommonTypes.TimeRange = {
  startTimeSecs: startTimeSeconds,
  endTimeSecs: endTimeSeconds
};

export const currentProcStageIntId = '3';

export const analystCurrentFk: AnalystCurrentFk = {
  x: 10,
  y: 11
};

export const eventIds = uniq(map([eventData], 'id'));

export const eventId = uniqBy([eventData], 'eventHypothesisId')[0].id;

export const singleEvent = uniqBy([eventData], 'eventHypothesisId')[0];

const sdIdsFullMap: string[] = signalDetectionsData.map(sd => sd.id);

export const signalDetectionsIds = uniq(sdIdsFullMap);

export const selectedSignalDetectionID = signalDetectionsIds[0];
export const testMagTypes: AnalystWorkspaceTypes.DisplayedMagnitudeTypes = {};
testMagTypes[LegacyEventTypes.MagnitudeType.MB] = true;
testMagTypes[LegacyEventTypes.MagnitudeType.MBMLE] = true;
testMagTypes[LegacyEventTypes.MagnitudeType.MS] = true;
testMagTypes[LegacyEventTypes.MagnitudeType.MSMLE] = true;

export const analystAppState: Partial<AppState> = {
  app: {
    userSession: {
      authenticationStatus: {
        userName: undefined,
        authenticated: false,
        authenticationCheckComplete: false,
        failedToConnect: false
      },
      connected: true
    },
    systemMessage: {
      lastUpdated: 0,
      latestSystemMessages: [],
      systemMessages: [],
      isSoundEnabled: false
    },
    stationPropertiesConfiguration: {
      channelConfigurationColumns: {
        name: true,
        effectiveAt: true,
        effectiveUntil: true,
        latitudeDegrees: true,
        longitudeDegrees: true,
        depthKm: true,
        elevationKm: true,
        nominalSampleRateHz: true,
        units: true,
        orientationHorizontalDegrees: true,
        orientationVerticalDegrees: true,
        calibrationFactor: true,
        calibrationPeriod: true,
        calibrationEffectiveAt: true,
        calibrationTimeShift: true,
        calibrationStandardDeviation: true,
        northDisplacementKm: true,
        eastDisplacementKm: true,
        verticalDisplacementKm: true,
        description: true,
        channelDataType: false,
        channelBandType: false,
        channelInstrumentType: false,
        channelOrientationCode: false,
        channelOrientationType: false,
        calibrationResponseId: true,
        fapResponseId: true
      },
      channelGroupConfigurationColumns: {
        name: true,
        effectiveAt: true,
        effectiveUntil: true,
        latitudeDegrees: true,
        longitudeDegrees: true,
        depthKm: true,
        elevationKm: true,
        type: true,
        description: true
      },
      selectedEffectiveAtIndex: 0
    },
    common: {
      commandPaletteIsVisible: false,
      keyPressActionQueue: {},
      keyboardShortcutsVisibility: false,
      selectedStationIds: undefined,
      glLayoutState: {}
    },
    analyst: {
      defaultSignalDetectionPhase: CommonTypes.PhaseType.P,
      selectedEventIds: eventIds,
      openEventId: eventId,
      hotkeyCycleOverrides: {},
      selectedSdIds: signalDetectionsIds,
      selectedFilterList: null,
      selectedFilterIndex: null,
      sdIdsToShowFk: [],
      selectedSortType: AnalystWorkspaceTypes.WaveformSortType.stationNameAZ,
      channelFilters: {},
      measurementMode: {
        mode: AnalystWorkspaceTypes.WaveformDisplayMode.MEASUREMENT,
        entries: {}
      },
      location: {
        selectedLocationSolutionSetId: undefined,
        selectedLocationSolutionId: undefined,
        selectedPreferredLocationSolutionSetId: undefined,
        selectedPreferredLocationSolutionId: undefined
      },
      historyActionInProgress: 1,
      openLayoutName: '',
      effectiveNowTime: null,
      eventListOpenEventTriggered: false,
      mapOpenEventTriggered: false,
      alignWaveformsOn: AlignWaveformsOn.TIME,
      phaseToAlignOn: undefined
    },

    workflow: {
      timeRange: null,
      stationGroup: null,
      openIntervalName: null,
      openActivityNames: [],
      analysisMode: null
    },
    events: {
      eventsColumns: {
        conflict: true,
        time: true,
        latitudeDegrees: true,
        longitudeDegrees: true,
        depthKm: true,
        region: true,
        confidenceSemiMajorAxis: false,
        confidenceSemiMinorAxis: false,
        coverageSemiMajorAxis: false,
        coverageSemiMinorAxis: false,
        magnitudeMb: true,
        magnitudeMs: false,
        magnitudeMl: false,
        activeAnalysts: true,
        preferred: true,
        status: true,
        rejected: true
      },
      edgeEvents: {
        'Edge events after interval': true,
        'Edge events before interval': true
      }
    },
    map: {
      isSyncedWithWaveformZoom: false,
      layerVisibility: {
        stations: true,
        sites: true,
        signalDetections: true,
        events: true,
        preferredLocationSolution: true,
        edgeEventsBeforeInterval: true,
        edgeEventsAfterInterval: true,
        nonPreferredLocationSolution: false,
        confidenceEllipse: false,
        coverageEllipse: true,

        edgeDetectionBefore: true,
        edgeDetectionAfter: true,

        unassociatedDetection: false,
        associatedOpenDetection: true,
        associatedOtherDetection: true,
        associatedCompleteDetection: true
      }
    },
    signalDetections: {
      displayedSignalDetectionConfiguration: {
        syncWaveform: false,
        signalDetectionBeforeInterval: true,
        signalDetectionAfterInterval: true,
        signalDetectionAssociatedToOpenEvent: true,
        signalDetectionAssociatedToCompletedEvent: true,
        signalDetectionAssociatedToOtherEvent: true,
        signalDetectionUnassociated: false
      },
      signalDetectionsColumns: {
        unsavedChanges: true,
        assocStatus: true,
        conflict: true,
        station: true,
        channel: true,
        phase: true,
        phaseConfidence: false,
        time: true,
        timeStandardDeviation: true,
        azimuth: true,
        azimuthStandardDeviation: true,
        slowness: true,
        slownessStandardDeviation: true,
        amplitude: true,
        period: true,
        sNR: true,
        rectilinearity: false,
        emergenceAngle: false,
        shortPeriodFirstMotion: false,
        longPeriodFirstMotion: false,
        rejected: true
      }
    },
    waveform: {
      shouldShowTimeUncertainty: false,
      shouldShowPredictedPhases: true,
      stationsVisibility: {},
      loadingState: DEFAULT_INITIAL_WAVEFORM_LOADING_STATE,
      viewableInterval: {
        startTimeSecs: 1,
        endTimeSecs: 100
      },
      zoomInterval: {
        startTimeSecs: 0,
        endTimeSecs: 99
      },
      minimumOffset: 0,
      maximumOffset: 0,
      baseStationTime: 0
    },
    dataAcquisition: {
      selectedAceiType: SohTypes.AceiType.BEGINNING_TIME_OUTAGE,
      selectedProcessingStation: undefined,
      unmodifiedProcessingStation: undefined,
      stationStatisticsGroup: 'ALL1',
      filtersToDisplay: {
        'soh-environment-monitor-statuses': {
          [FilterableSOHTypes.GOOD]: true,
          [FilterableSOHTypes.BAD]: true,
          [FilterableSOHTypes.MARGINAL]: true,
          [FilterableSOHTypes.NONE]: true
        },
        [Displays.SohDisplays.STATION_STATISTICS]: {
          [FilterableSOHTypes.GOOD]: true,
          [FilterableSOHTypes.BAD]: true,
          [FilterableSOHTypes.MARGINAL]: true,
          [FilterableSOHTypes.NONE]: true
        },
        [Displays.SohDisplays.SOH_OVERVIEW]: {
          [FilterableSOHTypes.GOOD]: true,
          [FilterableSOHTypes.BAD]: true,
          [FilterableSOHTypes.MARGINAL]: true,
          [FilterableSOHTypes.NONE]: true
        },
        [Displays.SohDisplays.SOH_LAG]: {
          [FilterableSOHTypes.GOOD]: true,
          [FilterableSOHTypes.BAD]: true,
          [FilterableSOHTypes.MARGINAL]: true
        },
        [Displays.SohDisplays.SOH_MISSING]: {
          [FilterableSOHTypes.GOOD]: true,
          [FilterableSOHTypes.BAD]: true,
          [FilterableSOHTypes.MARGINAL]: true
        },
        [Displays.SohDisplays.SOH_TIMELINESS]: {
          [FilterableSOHTypes.GOOD]: true,
          [FilterableSOHTypes.BAD]: true,
          [FilterableSOHTypes.MARGINAL]: true
        },
        [Displays.SohDisplays.SOH_TIMELINESS]: {
          [FilterableSOHTypes.GOOD]: true,
          [FilterableSOHTypes.BAD]: true,
          [FilterableSOHTypes.MARGINAL]: true
        },
        'soh-overview-groups': {
          ALL1: true,
          'CD1.1': true,
          ALL2: true
        }
      },
      data: {
        sohStatus: {
          lastUpdated: 0,
          loading: false,
          isStale: false,
          stationAndStationGroupSoh: {
            stationGroups: [],
            stationSoh: [],
            isUpdateResponse: false
          }
        }
      }
    }
  }
};

export const azSlowProps: AzimuthSlownessReduxProps = {
  location: undefined,
  currentTimeInterval: timeInterval,
  selectedSdIds: signalDetectionsIds,
  openEventId: eventId,
  sdIdsToShowFk: [],
  analysisMode: WorkflowTypes.AnalysisMode.EVENT_REVIEW,
  setSelectedSdIds: () => {
    logger.debug('azSlowProps - setSelectedSdIds');
  },
  setSdIdsToShowFk: () => {
    logger.debug('azSlowProps - setSdIdsToShowFk');
  },
  channelFilters: undefined,
  selectedSortType: undefined,
  setChannelFilters: jest.fn(),
  setMeasurementModeEntries: jest.fn(),
  defaultSignalDetectionPhase: undefined,
  unassociatedSDColor: ConfigurationTypes.defaultColorTheme.unassociatedSDColor
};

export const useQueryStateResult: UseQueryStateResult<any> = {
  isError: false,
  isFetching: false,
  isLoading: false,
  isSuccess: true,
  isUninitialized: true,
  currentData: undefined,
  data: undefined,
  endpointName: undefined,
  error: undefined,
  fulfilledTimeStamp: undefined,
  originalArgs: undefined,
  requestId: undefined,
  startedTimeStamp: undefined,
  status: undefined
};
