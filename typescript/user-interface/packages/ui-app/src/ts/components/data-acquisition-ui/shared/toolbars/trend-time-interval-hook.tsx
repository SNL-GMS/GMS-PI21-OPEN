import type { SohTypes } from '@gms/common-model';
import { millisToStringWithMaxPrecision } from '@gms/common-util';
import type { DateRangePickerTypes, DeprecatedToolbarTypes } from '@gms/ui-core-components';
import min from 'lodash/min';

import { useTimePicker } from '~common-ui/common/time-picker-control';

export const ACEI = 'ACEI';

/** Represents a type of Data for the Interval Selector */
export type TYPE = SohTypes.SohMonitorType | 'ACEI';

/** Defines a trend */
export interface Trend {
  /** the description of the trend ; i.e. `last 24 hours` */
  description: string;
  /** the number of milliseconds */
  value: number;
}

/** Returns the time interval selector tool tip text */
const getTimeIntervalSelectorTooltip = (): string => `History Interval`;

/** Returns the time interval selector label text */
const getTimeIntervalSelectorLabel = (): string => 'Set start and end times to display';

/**
 * Creates a custom React hook. This hook sets up and manages the state for
 * two toolbar items a drop down and an interval picker which work in sync with one another
 * for the historical trends toolbars. The drop down component is used to select
 * one of the default trends; and the interval picker is used to
 * set a custom or exact interval.
 *
 * @param type the type of data
 * @return Returns the the drop down item, interval picker item, and the start, and
 * end times.
 */
export const useTrendTimeIntervalSelector = (
  type: TYPE,
  sohHistoricalDurations: number[]
): [number, number, DeprecatedToolbarTypes.DateRangePickerItem] => {
  const now = Date.now();
  const defaultStart = now - min(sohHistoricalDurations);

  /**
   * Defines the default trend constants for
   * populating the trend drop downs.
   */
  const defaultTrends: DateRangePickerTypes.DurationOption[] = sohHistoricalDurations.map(
    duration => ({
      description: `Last ${millisToStringWithMaxPrecision(duration, 2)}`,
      value: duration
    })
  );

  const [timeRange, timePicker] = useTimePicker(
    {
      startTimeSecs: defaultStart / 1000,
      endTimeSecs: now / 1000
    },
    getTimeIntervalSelectorTooltip(),
    getTimeIntervalSelectorLabel(),
    1,
    defaultTrends
  );

  return [timeRange.startTimeSecs * 1000, timeRange.endTimeSecs * 1000, timePicker];
};
