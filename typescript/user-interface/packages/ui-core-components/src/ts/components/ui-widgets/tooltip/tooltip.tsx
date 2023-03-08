/* eslint-disable react/destructuring-assignment */
import { PopoverPosition, Tooltip } from '@blueprintjs/core';
import { Tooltip2 } from '@blueprintjs/popover2';
import * as React from 'react';

import type { Tooltip2Props, TooltipProps } from './types';

/** @deprecated use { Tooltip2Wrapper} */
export function TooltipWrapper<T>(props: React.PropsWithChildren<TooltipProps<T>>): JSX.Element {
  return (
    <Tooltip
      className={props.className || 'core-tooltip'}
      targetClassName={props.targetClassName || 'core-tooltip__target'}
      content={props.content}
      onOpened={props.onOpened}
      position={props.position || PopoverPosition.BOTTOM}
      wrapperTagName={props.wrapperTagName || 'div'}
      targetTagName={props.targetTagName || 'div'}
      hoverOpenDelay={300}
      boundary="window"
    >
      {props.children}
    </Tooltip>
  );
}

/**
 * Creates a Blueprint Tooltip2 around the children of this, with sensible defaults.
 */
export function Tooltip2Wrapper<T>(props: React.PropsWithChildren<Tooltip2Props<T>>): JSX.Element {
  return (
    <Tooltip2
      className={props.className || 'core-tooltip'}
      content={props.content}
      onOpened={props.onOpened}
      position={props.position || PopoverPosition.BOTTOM}
      targetTagName={props.targetTagName || 'div'}
      hoverOpenDelay={300}
    >
      {props.children}
    </Tooltip2>
  );
}
