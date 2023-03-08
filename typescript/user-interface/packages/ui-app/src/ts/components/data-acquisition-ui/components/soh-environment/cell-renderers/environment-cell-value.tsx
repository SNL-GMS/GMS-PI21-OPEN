import { Icon } from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import { ValueType } from '@gms/common-util';
import * as React from 'react';

import { formatSohValue } from '~components/data-acquisition-ui/shared/table/soh-cell-renderers';

import type { EnvironmentCellValueProps } from './types';

/**
 * Renders the value inside an environment cell. Formats it and applies a dirty dot if appropriate
 *
 * @param hasUnacknowledgedChanges determines if cell should render with dirty dot
 * @param value the value to format and display
 */
export function EnvironmentCellValue(props: React.PropsWithChildren<EnvironmentCellValueProps>) {
  const { hasUnacknowledgedChanges, value, children } = props;
  const formattedValue: number | string =
    value !== undefined && typeof value === 'number'
      ? formatSohValue(value, ValueType.PERCENTAGE, true) // returning as a string to keep e.x 12.0 not 12
      : 'Unknown';
  return (
    <div className="table-cell__value table-cell__value--numeric" data-cy="environment-cell">
      {hasUnacknowledgedChanges ? (
        <Icon
          icon={IconNames.SYMBOL_CIRCLE}
          className="env-dirty-dot"
          data-cy="environment-dirty-dot"
        />
      ) : null}
      <span>{formattedValue}</span>
      {children}
    </div>
  );
}
