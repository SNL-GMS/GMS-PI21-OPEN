import { IanDisplays } from '@gms/common-model/lib/displays/types';
import { useInterval } from '@gms/ui-state';
import React from 'react';

import type { IANMapComponentProps } from '~analyst-ui/components/map/types';
import { BaseDisplay } from '~common-ui/components/base-display';

import {
  useIsMapSyncedToWaveformZoom,
  useMapNonPreferredEventData,
  useMapPreferredEventData,
  useSignalDetectionForMap,
  useStationData
} from './ian-map-hooks';
import { IANMapPanel } from './ian-map-panel';

/**
 * IAN Map component. Renders a Cesium map and queries for Station Groups
 */
// eslint-disable-next-line react/function-component-definition
export const IANMapComponent: React.FunctionComponent<IANMapComponentProps> = (
  props: IANMapComponentProps
) => {
  const { glContainer } = props;
  const stationData = useStationData();
  const [interval] = useInterval(useIsMapSyncedToWaveformZoom());
  const signalDetectionData = useSignalDetectionForMap(interval);
  const preferredEventData = useMapPreferredEventData();
  const nonPreferredEventData = useMapNonPreferredEventData();
  const [stationCount, setStationCount] = React.useState<number>(0);
  const [signalDetectionCount, setSignalDetectionCount] = React.useState<number>(0);
  const [preferredEventCount, setPreferredEventCount] = React.useState<number>(0);
  const [nonPreferredEventCount, setNonPreferredEventCount] = React.useState<number>(0);

  const stationMount = React.useRef(() => {
    setStationCount(n => n + 1);
  });
  const signalDetectionMount = React.useRef(() => {
    setSignalDetectionCount(n => n + 1);
  });
  const preferredEventMount = React.useRef(() => {
    setPreferredEventCount(n => n + 1);
  });
  const nonPreferredEventMount = React.useRef(() => {
    setNonPreferredEventCount(n => n + 1);
  });
  return (
    <BaseDisplay
      glContainer={glContainer}
      tabName={IanDisplays.MAP}
      className="ian-map-gl-container"
      data-cy="ian-map-container"
      data-station-count={stationCount}
      data-signal-detection-count={signalDetectionCount}
      data-preferred-event-count={preferredEventCount}
      data-non-preferred-event-count={nonPreferredEventCount}
    >
      <IANMapPanel
        stationsResult={stationData}
        stationMount={stationMount.current}
        signalDetections={signalDetectionData}
        signalDetectionMount={signalDetectionMount.current}
        preferredEventsResult={preferredEventData}
        preferredEventMount={preferredEventMount.current}
        nonPreferredEventsResult={nonPreferredEventData}
        nonPreferredEventMount={nonPreferredEventMount.current}
      />
    </BaseDisplay>
  );
};

export const IANMap = React.memo(IANMapComponent);
