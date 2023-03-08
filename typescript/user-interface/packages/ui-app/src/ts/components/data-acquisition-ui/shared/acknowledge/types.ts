import type { SohConfigurationQueryProps, SohStatus } from '@gms/ui-state';

export interface AcknowledgeWrapperReduxProps {
  sohStatus: SohStatus;
}

export type AcknowledgeWrapperProps = SohConfigurationQueryProps & AcknowledgeWrapperReduxProps;

/**
 * The function signature that the WithAcknowledge component
 * passes to its child
 */
export interface WithAcknowledgeProps {
  acknowledgeStationsByName(stationNames: string[], comment?: string): void;
}

export interface AcknowledgeOverlayProps extends WithAcknowledgeProps {
  isOpen: boolean;
  stationNames: string[];
  requiresModificationForSubmit?: boolean;
  onClose(): void;
}
