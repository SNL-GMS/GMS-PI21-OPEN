import { IconNames } from '@blueprintjs/icons';
import type { SohTypes } from '@gms/common-model';
import { nonIdealStateWithNoSpinner, nonIdealStateWithSpinner } from '@gms/ui-core-components';
import type { UiHistoricalSohAsTypedArray } from '@gms/ui-state';

import { getLabel } from './utils';

/** Returns the loading non ideal state - used when the historical data query is loading */
const loading = (monitorType: SohTypes.SohMonitorType) =>
  nonIdealStateWithSpinner('Loading', `Historical ${getLabel(monitorType)}`);

/** Returns the error non ideal state for when the start and end times are in error */
const badStartEndTime = () =>
  nonIdealStateWithNoSpinner('Error', 'Invalid start and end times', IconNames.ERROR);

/** Returns the non ideal state that indicates that there is no historical data */
const noData = (monitorType: SohTypes.SohMonitorType) =>
  nonIdealStateWithNoSpinner('No Data', `No historical ${getLabel(monitorType)}`);

/** Returns the non ideal state that indicates that there is no historical data */
const error = (monitorType: SohTypes.SohMonitorType) =>
  nonIdealStateWithNoSpinner('Error', `Error querying for ${getLabel(monitorType)}`);

/**
 * Validates the non ideal state for the history trends components.
 * Returns the correct non ideal state if the condition is met.
 *
 * @param monitorType soh monitor type
 * @param isLoading loading status of the query
 * @param isError error status of the query
 * @param historicalSohByStation UI historical soh data from a query
 * @param startTimeMs the start time
 * @param endTimeMs the end time
 */
export const validateNonIdealState = (
  monitorType: SohTypes.SohMonitorType,
  isLoading: boolean,
  isError: boolean,
  historicalSohByStation: UiHistoricalSohAsTypedArray,
  startTimeMs: number,
  endTimeMs: number
): any => {
  if (isLoading) {
    return loading(monitorType);
  }

  if (isError) {
    return error(monitorType);
  }

  if (startTimeMs >= endTimeMs) {
    return badStartEndTime();
  }

  if (
    !historicalSohByStation ||
    !historicalSohByStation.calculationTimes ||
    historicalSohByStation.calculationTimes.length === 0 ||
    !historicalSohByStation.monitorValues ||
    historicalSohByStation.monitorValues.length === 0
  ) {
    return noData(monitorType);
  }

  return undefined;
};
