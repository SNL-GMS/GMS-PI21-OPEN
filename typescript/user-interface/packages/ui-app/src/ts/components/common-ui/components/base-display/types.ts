import type { IanDisplays } from '@gms/common-model/lib/displays/types';
import type GoldenLayout from '@gms/golden-layout';

/**
 * Props for the base display that goes as a direct child
 * of the golden layout display.
 * Base Display accepts data-* props, as well, which it will
 * apply to the div that it creates.
 *
 * For example
 * <BaseDisplay
 *  "data-cy": "example-cypress-tag"
 * >
 */
export interface BaseDisplayProps {
  glContainer: GoldenLayout.Container;
  tabName?: IanDisplays;
  className?: string;
  displayRef?: React.MutableRefObject<HTMLDivElement>;
  onContextMenu?(e: React.MouseEvent<HTMLElement, MouseEvent>): void;
}
