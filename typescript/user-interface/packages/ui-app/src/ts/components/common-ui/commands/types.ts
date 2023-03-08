import type GoldenLayout from '@gms/golden-layout';
import type { AuthenticationStatus } from '@gms/ui-state';

export interface CommandRegistrarReduxProps {
  setAppAuthenticationStatus(auth: AuthenticationStatus): void;
}

export interface CommandRegistrarBaseProps {
  glContainer?: GoldenLayout.Container;
}

export type CommandRegistrarProps = CommandRegistrarReduxProps & CommandRegistrarBaseProps;
