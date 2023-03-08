import type { FkTypes } from '@gms/common-model';
import type { CheckboxSearchListTypes } from '@gms/ui-core-components';

import type {
  FkConfigurationWithUnits,
  FkUnits
} from '~analyst-ui/components/azimuth-slowness/types';

export enum FkConfigurationPopoverPanel {
  DEFAULT,
  ADVANCED
}

export interface FkConfigurationPopoverState {
  openPanel: FkConfigurationPopoverPanel;
  fkUnits: FkUnits;
  normalizeWaveforms: boolean;
  mediumVelocity: number;
  maximumSlowness: number;
  numberOfPoints: number;
  channelCheckboxes: CheckboxSearchListTypes.CheckboxItem[];
  useVerticalChannelOffsets: boolean;
}
export interface FkConfigurationPopoverProps extends FkTypes.FkConfiguration {
  fkUnitDisplayed: FkUnits;
  applyFkConfiguration(configuration: FkConfigurationWithUnits);
  close();
}
