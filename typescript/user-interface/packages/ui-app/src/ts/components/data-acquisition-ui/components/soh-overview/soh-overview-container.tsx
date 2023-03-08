import { compose } from '@gms/common-util';
import { WithNonIdealStates } from '@gms/ui-core-components';

import { CommonNonIdealStateDefs } from '~components/common-ui/components/non-ideal-states';
import { WithAcknowledge } from '~components/data-acquisition-ui/shared/acknowledge';
import { DataAcquisitionNonIdealStateDefs } from '~components/data-acquisition-ui/shared/non-ideal-states';

import type { SohOverviewProps } from './soh-overview-component';
import { SohOverviewComponent, sohOverviewConnector } from './soh-overview-component';

/**
 * Renders the Overview component, or a non-ideal state
 */
const OverviewComponentOrNonIdealState = WithNonIdealStates<SohOverviewProps>(
  [
    ...CommonNonIdealStateDefs.baseNonIdealStateDefinitions,
    ...DataAcquisitionNonIdealStateDefs.generalSohNonIdealStateDefinitions
  ],
  WithAcknowledge(SohOverviewComponent)
);

/**
 * A new redux component that's wrapping the SohOverview component.
 */
export const SohOverviewContainer = compose(sohOverviewConnector)(OverviewComponentOrNonIdealState);
