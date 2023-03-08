import { SohTypes } from '@gms/common-model';
import { compose, ValueType } from '@gms/common-util';
import { WithNonIdealStates } from '@gms/ui-core-components';

import { CommonNonIdealStateDefs } from '~components/common-ui/components/non-ideal-states';
import { DataAcquisitionNonIdealStateDefs } from '~components/data-acquisition-ui/shared/non-ideal-states';

import type { Type } from './bar-chart/types';
import type { SohBarChartProps } from './soh-bar-chart-component';
import { SohBarChart, sohBarChartConnector } from './soh-bar-chart-component';

/**
 * Renders the SohBarChart component, or a non-ideal state from the provided list of
 * non ideal state definitions
 */
const SohBarChartComponentOrNonIdealState = WithNonIdealStates<SohBarChartProps>(
  [
    ...CommonNonIdealStateDefs.baseNonIdealStateDefinitions,
    ...DataAcquisitionNonIdealStateDefs.generalSohNonIdealStateDefinitions,
    ...DataAcquisitionNonIdealStateDefs.stationSelectedSohNonIdealStateDefinitions,
    ...DataAcquisitionNonIdealStateDefs.channelSohNonIdealStateDefinitions
  ],
  SohBarChart
);

const SohBarChartComponent = (type: Type, valueType: ValueType) =>
  compose(sohBarChartConnector(type, valueType))(SohBarChartComponentOrNonIdealState);

export const SohLag = SohBarChartComponent(SohTypes.SohMonitorType.LAG, ValueType.FLOAT);

export const SohMissing = SohBarChartComponent(
  SohTypes.SohMonitorType.MISSING,
  ValueType.PERCENTAGE
);

export const SohTimeliness = SohBarChartComponent(
  SohTypes.SohMonitorType.TIMELINESS,
  ValueType.FLOAT
);
