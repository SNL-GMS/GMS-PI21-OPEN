import type { ITooltipProps } from '@blueprintjs/core';
import type { Tooltip2Props as ITooltip2Props } from '@blueprintjs/popover2';

export type TooltipProps<T> = T & ITooltipProps;

export type Tooltip2Props<T> = T & ITooltip2Props;
