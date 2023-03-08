import type { SohStatus } from '@gms/ui-state';

import type { WithAcknowledgeProps } from '../shared/acknowledge';

export interface CommandRegistrarBaseProps {
  selectedStationIds: string[];
  sohStatus: SohStatus;
  setSelectedStationIds(ids: string[]): void;
}

export type CommandRegistrarProps = CommandRegistrarBaseProps & WithAcknowledgeProps;
