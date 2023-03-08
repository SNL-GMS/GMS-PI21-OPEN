import React from 'react';
import { HelpTextRenderer } from '../../components/HelpTextRenderer';
import { useResolveStationConfig } from '../../state/api-slice';
import { useAppDispatch, useAppSelector } from '../../state/react-redux-hooks';
import {
  setBackOffDuration,
  setCalculationInterval,
} from '../../state/station-controls-slice';
import { ISOTimeInput } from '../iso-time-input/ISOTimeInput';

interface StationDurationsProps {
  options?: string[];
  className?: string;
  label?: string;
  description?: string;
}

/**
 * A function that allows inputs to back off duration and calculation interval
 * Updates redux state on changes and gets it's 'options' for duration suggestions
 * from schema
 *
 * @returns two ISOTimeInputs for Station Timewindows
 */
export const StationDurations: React.FC<StationDurationsProps> = ({
  className,
  label,
  options,
  description,
}: StationDurationsProps) => {
  const stationName = useAppSelector(
    (state) => state.stationControls.stationName
  );

  const backOffDuration = useAppSelector(
    (state) => state.stationControls.backOffDuration[stationName ?? '']
  );
  const calculationInterval = useAppSelector(
    (state) => state.stationControls.calculationInterval[stationName ?? '']
  );
  const resolvedMonitorTimewindowsConfig = useResolveStationConfig(
    {
      configName: 'soh-control.soh-monitor-timewindows',
      stationName: stationName ?? '',
    },
    { skip: !stationName }
  );

  const dispatch = useAppDispatch();

  const updateBackOffDuration = React.useCallback(
    (value: string) => {
      dispatch(setBackOffDuration({ stationName, backOffDuration: value }));
    },
    [dispatch, stationName]
  );

  const updateCalculationInterval = React.useCallback(
    (value: string) => {
      dispatch(
        setCalculationInterval({ stationName, calculationInterval: value })
      );
    },
    [dispatch, stationName]
  );

  const hasLoadedDataIntoState = React.useRef(false);
  React.useEffect(() => {
    if (
      stationName != null &&
      resolvedMonitorTimewindowsConfig.data &&
      !hasLoadedDataIntoState.current
    ) {
      hasLoadedDataIntoState.current = true;
      updateBackOffDuration(
        resolvedMonitorTimewindowsConfig.data.backOffDuration
      );
      updateCalculationInterval(
        resolvedMonitorTimewindowsConfig.data.calculationInterval
      );
    }
  }, [
    resolvedMonitorTimewindowsConfig.data,
    stationName,
    updateBackOffDuration,
    updateCalculationInterval,
  ]);
  return (
    <>
      <HelpTextRenderer
        helpText={
          'Accounts for normal latency of system in which data is not expected to be received. Data more recent than the current time minus the Back Off Duration is not included in the SOH Calculations (except for the Timeliness calculations which do not use the Back Off Duration). '
        }
        isLoading={resolvedMonitorTimewindowsConfig.isLoading}
      >
        <ISOTimeInput
          label='Back Off Duration'
          data={backOffDuration ?? ''}
          canInputBeZero={true}
          options={options ?? []}
          update={updateBackOffDuration}
        />
      </HelpTextRenderer>
      <HelpTextRenderer
        helpText={
          'Length of time over which calculations will be made. Only data timestamped between the current time minus the back off interval minus the calculation interval and the current time minus the back off duration is used for SOH Calculations (except for the Timeliness calculation which do not use the Calculation Interval). '
        }
        isLoading={resolvedMonitorTimewindowsConfig.isLoading}
      >
        <ISOTimeInput
          label='Calculation Interval'
          data={calculationInterval ?? ''}
          canInputBeZero={false}
          options={options ?? []}
          update={updateCalculationInterval}
        />
      </HelpTextRenderer>
    </>
  );
};
