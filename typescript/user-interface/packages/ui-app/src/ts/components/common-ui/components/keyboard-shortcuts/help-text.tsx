import { Icon } from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import { Tooltip2Wrapper } from '@gms/ui-core-components';
import React from 'react';

/**
 * Component to create a tooltip icon that appears on hover, and that adds
 * the help text to the tooltip.
 */
// eslint-disable-next-line react/function-component-definition
export const HelpText: React.FC<{ children: React.ReactNode & (string | JSX.Element) }> = ({
  children
}: {
  children: React.ReactNode & (string | JSX.Element);
}) => {
  return (
    <Tooltip2Wrapper
      className="keyboard-shortcuts__help-text"
      content={<p className="keyboard-shortcuts__help-text--wrap">{children}</p>}
    >
      <Icon icon={IconNames.INFO_SIGN} size={14} />
    </Tooltip2Wrapper>
  );
};
