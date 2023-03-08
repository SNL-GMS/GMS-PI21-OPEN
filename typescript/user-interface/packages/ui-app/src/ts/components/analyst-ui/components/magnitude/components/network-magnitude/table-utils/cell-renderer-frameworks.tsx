/* eslint-disable react/destructuring-assignment */
/* eslint-disable react/prop-types */
import { Icon, Intent } from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import React from 'react';

// eslint-disable-next-line react/function-component-definition
export const SetCircleTick: React.FunctionComponent<any> = props =>
  props.data.isPreferred ? <Icon icon={IconNames.TICK_CIRCLE} intent={Intent.PRIMARY} /> : <div />;
