import { compose } from '@gms/common-util';
import type React from 'react';
import * as ReactRedux from 'react-redux';

import type { WeavessDisplayComponentProps } from './types';
import { WeavessDisplayComponent } from './weavess-display-component';

/**
 * higher-order component WeavessDisplay
 */
export const WeavessDisplay: React.ComponentClass<WeavessDisplayComponentProps, never> = compose(
  ReactRedux.connect(null, null, null, { forwardRef: true })
)(WeavessDisplayComponent);
