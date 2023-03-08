/* eslint-disable react/prop-types */
/* eslint-disable react/destructuring-assignment */
import type { ConfigurationTypes } from '@gms/common-model';
import { Displays, SohTypes } from '@gms/common-model';
import type { ValueType } from '@gms/common-util';
import type GoldenLayout from '@gms/golden-layout';
import type { FilterLists, SohStatus } from '@gms/ui-state';
import { useStatusesToDisplay } from '@gms/ui-state';
import { useElementSize } from '@gms/ui-util';
import * as React from 'react';

import type { FilterableSOHTypes } from '~components/data-acquisition-ui/shared/toolbars/soh-toolbar';

import { BarChartPanel } from './bar-chart/bar-chart-panel';
import { getChannelSoh, useSortDropdown } from './bar-chart/bar-chart-utils';
import type { Type } from './bar-chart/types';
import { DeprecatedToolbar } from './soh-bar-chart-toolbar';

const DEFAULT_TOP_HEIGHT_PX = 72;

function getDisplayFromType(type: Type): FilterLists {
  if (type === SohTypes.SohMonitorType.TIMELINESS) {
    return Displays.SohDisplays.SOH_TIMELINESS;
  }
  if (type === SohTypes.SohMonitorType.LAG) {
    return Displays.SohDisplays.SOH_LAG;
  }
  if (type === SohTypes.SohMonitorType.MISSING) {
    return Displays.SohDisplays.SOH_MISSING;
  }
  throw new Error('Invalid display type for SOH Bar Chart Panel');
}

/**
 * SohBarChartPanelProps props
 */
export interface SohBarChartPanelProps {
  minHeightPx: number;
  type: Type;
  station: SohTypes.UiStationSoh;
  sohStatus: SohStatus;
  sohConfiguration: ConfigurationTypes.UiSohConfiguration;
  valueType: ValueType;
  glContainer?: GoldenLayout.Container;
}

// eslint-disable-next-line react/function-component-definition
export const SohBarChartPanel: React.FunctionComponent<SohBarChartPanelProps> = props => {
  const [sortDropdown, getSortFunction] = useSortDropdown(props.type);
  const display = getDisplayFromType(props.type);
  // Typescript does't reason well about subtraction types, and so we can't pass in FilterableSOHTypes here
  const [statusesToDisplay, setStatusesToDisplay] = useStatusesToDisplay<any>(display);

  const channelSoh = getChannelSoh(props.type, props.station)
    .sort(getSortFunction())
    .filter(c => statusesToDisplay[(c.status as unknown) as FilterableSOHTypes]);

  const [headerRef] = useElementSize();

  const minHeight = Number(props.minHeightPx) + Number(DEFAULT_TOP_HEIGHT_PX);

  return (
    <div className="missing-wrapper" style={{ minHeight }}>
      {
        // !TODO CHANGE THE CLASS NAME ABOVE
      }
      <DeprecatedToolbar
        statusesToDisplay={statusesToDisplay}
        setStatusesToDisplay={setStatusesToDisplay}
        sortDropdown={sortDropdown}
        forwardRef={headerRef}
        station={props.station}
        monitorType={props.type}
      />
      <BarChartPanel
        key={`${props.type}${props.valueType}`}
        minHeightPx={props.minHeightPx}
        glContainer={props.glContainer}
        chartHeaderHeight={DEFAULT_TOP_HEIGHT_PX}
        type={props.type}
        sohStatus={props.sohStatus}
        station={props.station}
        channelSoh={channelSoh}
        sohConfiguration={props.sohConfiguration}
        valueType={props.valueType}
      />
    </div>
  );
};
