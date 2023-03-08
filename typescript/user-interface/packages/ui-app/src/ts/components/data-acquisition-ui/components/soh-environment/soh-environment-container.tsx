import { compose } from '@gms/common-util';
import { WithNonIdealStates } from '@gms/ui-core-components';

import { CommonNonIdealStateDefs } from '~components/common-ui/components/non-ideal-states';
import { DataAcquisitionNonIdealStateDefs } from '~components/data-acquisition-ui/shared/non-ideal-states';

import type { EnvironmentProps } from './soh-environment-component';
import { EnvironmentComponent, sohEnvironmentConnector } from './soh-environment-component';

/**
 * Renders the Environment display, or a non-ideal state from the provided list of
 * non ideal state definitions
 */
const EnvironmentComponentOrNonIdealState = WithNonIdealStates<EnvironmentProps>(
  [
    ...CommonNonIdealStateDefs.baseNonIdealStateDefinitions,
    ...DataAcquisitionNonIdealStateDefs.generalSohNonIdealStateDefinitions,
    ...DataAcquisitionNonIdealStateDefs.stationSelectedSohNonIdealStateDefinitions,
    ...DataAcquisitionNonIdealStateDefs.channelSohNonIdealStateDefinitions
  ],
  EnvironmentComponent
);

/**
 * A new component that's wrapping the component
 */
export const EnvironmentContainer = compose(sohEnvironmentConnector)(
  EnvironmentComponentOrNonIdealState
);
