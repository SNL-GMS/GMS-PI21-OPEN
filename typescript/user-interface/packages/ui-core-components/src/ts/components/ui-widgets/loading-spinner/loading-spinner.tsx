/* eslint-disable react/prop-types */
/* eslint-disable react/destructuring-assignment */
import { Intent, Spinner, SpinnerSize } from '@blueprintjs/core';
import React from 'react';

import type { LoadingSpinnerProps } from './types';
// A loading spinner widget to be used in toolbars the world over
// eslint-disable-next-line react/function-component-definition
export const LoadingSpinner: React.FunctionComponent<LoadingSpinnerProps> = props => (
  <div
    className="loading-spinner__container"
    style={{
      minWidth: `${props.widthPx}px`
    }}
  >
    {props.itemsToLoad > 0 ? (
      <span>
        <Spinner
          intent={Intent.PRIMARY}
          size={SpinnerSize.SMALL}
          value={props.itemsLoaded ? props.itemsLoaded / props.itemsToLoad : undefined}
        />
        {props.onlyShowSpinner ? null : (
          <span>
            {props.hideTheWordLoading ? '' : 'Loading'}
            {props.hideOutstandingCount ? props.itemsToLoad : ''}
            ...
          </span>
        )}
      </span>
    ) : null}
  </div>
);
