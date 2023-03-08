import { IanDisplays } from '@gms/common-model/lib/displays/types';
import type GoldenLayout from '@gms/golden-layout';
import { UILogger } from '@gms/ui-util';
import * as React from 'react';

import { BaseDisplay } from '~common-ui/components/base-display';

import { FiltersPanel } from './filters-panel';

const logger = UILogger.create('GMS_LOG_FILTERS', process.env.GMS_LOG_FILTERS);

export interface FiltersComponentProps {
  // passed in from golden-layout
  readonly glContainer?: GoldenLayout.Container;
}

// eslint-disable-next-line react/function-component-definition
export const FiltersComponent: React.FunctionComponent<FiltersComponentProps> = (
  props: FiltersComponentProps
) => {
  logger.debug(`Rendering FiltersComponent`, props);
  const { glContainer } = props;

  return (
    <BaseDisplay
      glContainer={glContainer}
      className="filters-display-window gms-body-text"
      data-cy="filters-display-window"
      tabName={IanDisplays.FILTERS}
    >
      <FiltersPanel />
    </BaseDisplay>
  );
};
