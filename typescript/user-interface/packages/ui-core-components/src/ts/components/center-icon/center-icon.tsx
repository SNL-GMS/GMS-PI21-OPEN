/* eslint-disable react/prop-types */
import { Icon } from '@blueprintjs/core';
import type { IconNames } from '@blueprintjs/icons';
import * as React from 'react';

export type IconName = false | JSX.Element | typeof IconNames[keyof typeof IconNames];

/**
 * Creates an icon with label used when there is no station that need attention
 *
 * @prams a blue print icon name from IconNames
 * @prams description as string goes under the icon
 * @prams iconSize as in number in px
 * @prams color as a string
 * @prams className as a string
 */
export const CenterIcon: React.FunctionComponent<{
  iconName: IconName;
  description: string;
  iconSize: number;
  color?: string;
  className?: string;
  // eslint-disable-next-line react/function-component-definition
}> = ({ iconName, description, iconSize, color, className }) => (
  <div
    className={`center-children orientation-column fill-all-space space-between-children ${className}`}
  >
    <Icon icon={iconName} title={false} iconSize={iconSize} color={color || ''} />
    <p>{`${description}`}</p>
  </div>
);
