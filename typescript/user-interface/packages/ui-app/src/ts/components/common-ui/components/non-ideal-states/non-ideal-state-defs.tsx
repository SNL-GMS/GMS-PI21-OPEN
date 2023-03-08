import type GoldenLayout from '@gms/golden-layout';
import type { NonIdealStateDefinition } from '@gms/ui-core-components';
import { nonIdealStateWithNoSpinner } from '@gms/ui-core-components';

export const baseNonIdealStateDefinitions: NonIdealStateDefinition<{
  glContainer?: GoldenLayout.Container;
}>[] = [
  {
    condition: props => props.glContainer && props.glContainer.isHidden,
    element: nonIdealStateWithNoSpinner()
  }
];
