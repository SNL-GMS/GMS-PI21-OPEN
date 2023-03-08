import { IconNames } from '@blueprintjs/icons';
import type { SohTypes } from '@gms/common-model';
import { nonIdealStateWithNoSpinner, nonIdealStateWithSpinner } from '@gms/ui-core-components';

/** Returns the loading non ideal state - used when the historical data query is loading */
const loading = () => nonIdealStateWithSpinner('Loading', `Historical data`);

/** Returns the error non ideal state for when the start and end times are in error */
const badStartEndTime = () =>
  nonIdealStateWithNoSpinner('Error', 'Invalid start and end times', IconNames.ERROR);

/** Returns the non ideal state that indicates that there is no historical data */
const noData = () => nonIdealStateWithNoSpinner('No Data', `No historical environmental data`);

/** Returns the non ideal state that indicates that no monitor type is selected */
const noMonitorSelected = () =>
  nonIdealStateWithNoSpinner('No Monitor Selected', 'Select an environmental monitor type');

/**
 * Validates the non ideal state for the acei query component.
 * Returns the correct non ideal state if the condition is met.
 *
 * @param props the props
 * @param context the query context (the historical query data)
 * @param startTimeMs the start time
 * @param endTimeMs the end time
 */
export const validateNonIdealState = (
  selectedMonitorType: SohTypes.AceiType,
  isLoading: boolean,
  startTimeMs: number,
  endTimeMs: number,
  data: SohTypes.UiHistoricalAcei[]
): any => {
  if (selectedMonitorType === undefined) {
    return noMonitorSelected();
  }

  if (isLoading) {
    return loading();
  }

  if (startTimeMs > endTimeMs) {
    return badStartEndTime();
  }

  if (!data || data.length < 1) {
    return noData();
  }

  return undefined;
};
