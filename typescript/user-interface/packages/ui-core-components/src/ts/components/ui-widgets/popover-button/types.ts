import type { IconName } from '@blueprintjs/core';

export interface PopoverProps {
  label: string;
  popupContent: JSX.Element;
  renderAsMenuItem?: boolean;
  disabled?: boolean;
  tooltip: string;
  cyData?: string;
  widthPx?: number;
  onlyShowIcon?: boolean;
  icon?: IconName;
  onPopoverDismissed?();
  onClick?(ref?: HTMLDivElement);
}
export interface PopoverState {
  isExpanded: boolean;
}
