import { compose } from '@gms/common-util';
import { WithNonIdealStates } from '@gms/ui-core-components';

import { CommonNonIdealStateDefs } from '~common-ui/components/non-ideal-states';
import { WithAcknowledge } from '~components/data-acquisition-ui/shared/acknowledge';
import { DataAcquisitionNonIdealStateDefs } from '~components/data-acquisition-ui/shared/non-ideal-states';

import type { StationStatisticsProps } from './station-statistics-component';
import {
  StationStatisticsComponent,
  stationStatisticsConnector
} from './station-statistics-component';

/**
 * Renders the station statistics display, or a non-ideal state from the provided list of
 * non ideal state definitions
 */
const StationStatisticsComponentOrNonIdealState = WithNonIdealStates<StationStatisticsProps>(
  [
    ...CommonNonIdealStateDefs.baseNonIdealStateDefinitions,
    ...DataAcquisitionNonIdealStateDefs.generalSohNonIdealStateDefinitions
  ],
  WithAcknowledge(StationStatisticsComponent)
);

/**
 * A new component that's wrapping the Station Statistics.
 */
export const StationStatisticsContainer = compose(stationStatisticsConnector)(
  StationStatisticsComponentOrNonIdealState
);
