import type { CommonTypes, SignalDetectionTypes } from '@gms/common-model';
import type { NonIdealStateDefinition } from '@gms/ui-core-components';
import { nonIdealStateWithNoSpinner, nonIdealStateWithSpinner } from '@gms/ui-core-components';
import type {
  EventsFetchResult,
  OperationalTimePeriodConfigurationQueryProps,
  ProcessingAnalystConfigurationQueryProps,
  ProcessingStationGroupNamesConfigurationQueryProps,
  SignalDetectionFetchResult,
  StationGroupsByNamesQueryProps,
  StationQueryProps
} from '@gms/ui-state';

/**
 * Non ideal state definitions for processingAnalystConfiguration query
 */
export const processingAnalystConfigNonIdealStateDefinitions: NonIdealStateDefinition<unknown>[] = [
  {
    condition: (props: ProcessingAnalystConfigurationQueryProps): boolean => {
      return props.processingAnalystConfigurationQuery?.isLoading;
    },
    element: nonIdealStateWithSpinner('Loading', 'Default Configuration')
  },
  {
    condition: (props: ProcessingAnalystConfigurationQueryProps): boolean => {
      return props.processingAnalystConfigurationQuery?.isError;
    },
    element: nonIdealStateWithSpinner('Error', 'Problem Loading Default Configuration')
  }
];

/**
 * Non ideal state definitions for operationalTimePeriodConfiguration query
 */
export const operationalTimePeriodConfigNonIdealStateDefinitions: NonIdealStateDefinition<
  unknown
>[] = [
  {
    condition: (props: OperationalTimePeriodConfigurationQueryProps): boolean => {
      return props.operationalTimePeriodConfigurationQuery?.isLoading;
    },
    element: nonIdealStateWithSpinner('Loading', 'Operational Time Period Configuration')
  },
  {
    condition: (props: OperationalTimePeriodConfigurationQueryProps): boolean => {
      return props.operationalTimePeriodConfigurationQuery?.isError;
    },
    element: nonIdealStateWithSpinner('Error', 'Operational Time Period Configuration')
  },
  {
    condition: (props: OperationalTimePeriodConfigurationQueryProps): boolean => {
      return (
        props.operationalTimePeriodConfigurationQuery?.isSuccess &&
        (props.operationalTimePeriodConfigurationQuery.data?.operationalPeriodStart === undefined ||
          props.operationalTimePeriodConfigurationQuery.data?.operationalPeriodStart === null)
      );
    },
    element: nonIdealStateWithNoSpinner(
      'Error',
      'Operational Time Period Configuration - invalid start not configured'
    )
  },
  {
    condition: (props: OperationalTimePeriodConfigurationQueryProps): boolean => {
      return (
        props.operationalTimePeriodConfigurationQuery?.isSuccess &&
        props.operationalTimePeriodConfigurationQuery.data?.operationalPeriodEnd == null
      );
    },
    element: nonIdealStateWithNoSpinner(
      'Error',
      'Operational Time Period Configuration - invalid end not configured'
    )
  },
  {
    condition: (props: OperationalTimePeriodConfigurationQueryProps): boolean => {
      return (
        props.operationalTimePeriodConfigurationQuery?.isSuccess &&
        props.operationalTimePeriodConfigurationQuery.data?.operationalPeriodStart ===
          props.operationalTimePeriodConfigurationQuery.data?.operationalPeriodEnd
      );
    },
    element: nonIdealStateWithNoSpinner(
      'Error',
      'Operational Time Period Configuration - invalid start and end equal'
    )
  }
];

/**
 * Non ideal state definitions for station group query
 */
export const processingStationGroupNamesConfigurationQueryNonIdealStateDefinitions: NonIdealStateDefinition<
  unknown
>[] = [
  {
    condition: (props: ProcessingStationGroupNamesConfigurationQueryProps): boolean => {
      return props.processingStationGroupNamesConfigurationQuery?.isLoading;
    },
    element: nonIdealStateWithSpinner('Loading', 'Station GroupNames Configuration')
  },
  {
    condition: (props: ProcessingStationGroupNamesConfigurationQueryProps): boolean => {
      return props.processingStationGroupNamesConfigurationQuery?.isError;
    },
    element: nonIdealStateWithSpinner('Error', 'Problem Loading Station Group Names Configuration')
  }
];

/**
 * Non ideal state definitions for station group query
 */
export const stationGroupQueryNonIdealStateDefinitions: NonIdealStateDefinition<unknown>[] = [
  {
    condition: (props: StationGroupsByNamesQueryProps): boolean => {
      return props.stationsGroupsByNamesQuery?.isLoading;
    },
    element: nonIdealStateWithSpinner('Loading', 'Station Groups')
  },
  {
    condition: (props: StationGroupsByNamesQueryProps): boolean => {
      return props.stationsGroupsByNamesQuery?.isError;
    },
    element: nonIdealStateWithSpinner('Error', 'Problem Loading Station Groups')
  }
];

/**
 * Non ideal state definitions for events query
 */
export const eventNonIdealStateDefinitions: NonIdealStateDefinition<{
  eventResults: EventsFetchResult;
}>[] = [
  {
    condition: (props: { eventResults: EventsFetchResult }): boolean => {
      return (
        props.eventResults?.isLoading ||
        props.eventResults?.pending !== 0 ||
        props.eventResults?.fulfilled === 0
      );
    },
    element: nonIdealStateWithSpinner('Loading', 'Events')
  },
  {
    condition: (props: { eventResults: EventsFetchResult }): boolean => {
      return props.eventResults?.isError;
    },
    element: nonIdealStateWithSpinner('Error', 'Problem Loading Events')
  }
];

/**
 * Non ideal state definitions for signalDetection query
 */
export const signalDetectionsNonIdealStateDefinitions: NonIdealStateDefinition<
  {
    signalDetectionResults: SignalDetectionFetchResult;
  },
  {
    signalDetections: SignalDetectionTypes.SignalDetection[];
  }
>[] = [
  {
    condition: (props: { signalDetectionResults: SignalDetectionFetchResult }): boolean => {
      return props.signalDetectionResults.isError;
    },
    element: nonIdealStateWithSpinner('Error', 'Problem Loading Signal Detections'),
    converter: (props: {
      signalDetectionResults: SignalDetectionFetchResult;
    }): {
      signalDetections: SignalDetectionTypes.SignalDetection[];
    } => {
      return {
        ...props,
        signalDetections: props.signalDetectionResults.data
      };
    }
  }
];

/**
 * Non ideal state definitions for waveform intervals: viewableInterval and zoomInterval
 */
export const waveformIntervalsNonIdealStateDefinitions: NonIdealStateDefinition<unknown>[] = [
  {
    condition: (props: { viewableInterval: CommonTypes.TimeRange }): boolean => {
      return !props.viewableInterval;
    },
    element: nonIdealStateWithSpinner('Initializing', 'Waveform Viewable Interval')
  }
];

/**
 * Non ideal state definitions for station definition query
 */
export const stationDefinitionNonIdealStateDefinitions: NonIdealStateDefinition<unknown>[] = [
  {
    condition: (props: StationQueryProps): boolean => {
      return props.stationsQuery?.isLoading;
    },
    element: nonIdealStateWithSpinner('Loading', 'Station Definitions')
  },
  {
    condition: (props: StationQueryProps): boolean => {
      return props.stationsQuery?.isError;
    },
    element: nonIdealStateWithNoSpinner('Error', 'Loading Station Definitions')
  }
];

/**
 * Non-ideal state definition for checking that a time range is set
 *
 * @param description optional string to represent data description to be displayed; defaults to `data`
 * @param field optional string to represent prop field to check the time range on; defaults to `timeRange`
 * @returns non ideal state
 */
export const timeRangeNonIdealStateDefinitions = (
  description = 'data',
  field = 'timeRange'
): NonIdealStateDefinition<unknown>[] => {
  return [
    {
      condition: (props: unknown): boolean => {
        return (
          field === undefined ||
          field === null ||
          typeof props[field] === 'undefined' ||
          props[field] === undefined ||
          props[field] === null ||
          props[field].endTimeSecs === undefined ||
          props[field].endTimeSecs === null ||
          props[field].startTimeSecs === undefined ||
          props[field].startTimeSecs === null
        );
      },
      element: nonIdealStateWithNoSpinner(
        'No Interval Selected',
        `Select an interval in the Workflow Display to view ${description}`,
        'select'
      )
    }
  ];
};
