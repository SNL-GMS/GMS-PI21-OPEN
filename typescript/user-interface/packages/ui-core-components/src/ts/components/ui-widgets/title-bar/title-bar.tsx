/* eslint-disable react/prop-types */
/* eslint-disable react/destructuring-assignment */
import { H4 } from '@blueprintjs/core';
import React from 'react';

import type { TitleBarProps } from './types';

// eslint-disable-next-line react/function-component-definition
export const TitleBar: React.FunctionComponent<React.PropsWithChildren<TitleBarProps>> = props => (
  <div className={`top-bar ${props.className ? props.className : ''}`}>
    <H4 className="top-bar__title">{props.title}</H4>
    {props.children}
  </div>
);
