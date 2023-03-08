import type { SohTypes } from '@gms/common-model';
import { compose } from '@gms/common-util';
import type { NonIdealStateDefinition } from '@gms/ui-core-components';
import { nonIdealStateWithNoSpinner, WithNonIdealStates } from '@gms/ui-core-components';

import { CommonNonIdealStateDefs } from '~components/common-ui/components/non-ideal-states';
import { DataAcquisitionNonIdealStateDefs } from '~components/data-acquisition-ui/shared/non-ideal-states';
import { isAnalogAceiMonitorType } from '~components/data-acquisition-ui/shared/utils';

import type { EnvironmentHistoryProps } from './environment-history-component';
import {
  EnvironmentHistoryComponent,
  environmentHistoryConnector
} from './environment-history-component';

// TODO: Remove this when analog monitor types are supported
/**
 * A non ideal state for unsupported analog monitor types
 */
const analogMonitorTypeNonIdealStateDefinition: NonIdealStateDefinition<{
  selectedAceiType: SohTypes.AceiType;
}> = {
  condition: props => isAnalogAceiMonitorType(props.selectedAceiType),
  element: nonIdealStateWithNoSpinner(
    'Unsupported monitor type',
    'Analog environmental monitor types not supported at this time. Select a boolean monitor type to see historical trends.'
  )
};

/**
 * Renders the Environment History display, or a non-ideal state from the provided list of
 * non ideal state definitions
 */
const EnvironmentHistoryComponentOrNonIdealState = WithNonIdealStates<EnvironmentHistoryProps>(
  [
    ...CommonNonIdealStateDefs.baseNonIdealStateDefinitions,
    ...DataAcquisitionNonIdealStateDefs.generalSohNonIdealStateDefinitions,
    ...DataAcquisitionNonIdealStateDefs.stationSelectedSohNonIdealStateDefinitions,
    ...DataAcquisitionNonIdealStateDefs.channelSohNonIdealStateDefinitions,
    analogMonitorTypeNonIdealStateDefinition
  ],
  EnvironmentHistoryComponent
);

/**
 * A new component that's wrapping the component and injecting.
 */
export const EnvironmentContainer = compose(environmentHistoryConnector)(
  EnvironmentHistoryComponentOrNonIdealState
);
