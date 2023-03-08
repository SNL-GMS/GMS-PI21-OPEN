/* eslint-disable react/prop-types */
/* eslint-disable react/destructuring-assignment */
import { Button, Collapse } from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import { classList } from '@gms/ui-util';
import * as React from 'react';

import type { CollapseButtonProps } from '.';

export const CollapseButton: React.FunctionComponent<React.PropsWithChildren<
  CollapseButtonProps
  // eslint-disable-next-line react/function-component-definition
>> = props => {
  const [isVisible, setIsVisible] = React.useState(props.isCollapsed ?? false);
  const toggleIsCollapsed = () => {
    // capture the value because setIsVisible does not update synchronously
    const newVisibility = !isVisible;
    setIsVisible(newVisibility);
    props.onClick(newVisibility);
  };
  const buttonIcon = isVisible ? IconNames.CARET_UP : IconNames.CARET_DOWN;
  return (
    <>
      <Button
        className={classList(
          {
            'collapse-button--closed': !isVisible,
            'collapse-button--open': isVisible
          },
          'button--full-width'
        )}
        onClick={toggleIsCollapsed}
        loading={props.isLoading}
        rightIcon={buttonIcon}
      >
        {typeof props.buttonText === 'function' ? props.buttonText(isVisible) : props.buttonText}
      </Button>
      <Collapse
        className={classList(
          {
            'collapse-button__target--closed': !isVisible,
            'collapse-button__target--open': isVisible
          },
          'button--full-width'
        )}
        isOpen={isVisible}
      >
        {props.children}
      </Collapse>
    </>
  );
};
