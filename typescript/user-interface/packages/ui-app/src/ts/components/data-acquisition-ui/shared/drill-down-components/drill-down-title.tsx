import * as React from 'react';

import type { DrillDownTitleProps } from './types';

export function DrillDownTitle(props: DrillDownTitleProps) {
  const { title, subtitle, description } = props;
  return (
    <div className="soh-drill-down-station-label display-title" data-cy="drill-down-display-title">
      {title}
      <div className="display-title__subtitle">
        {subtitle}
        {description ? ` (${description})` : ''}
      </div>
    </div>
  );
}
