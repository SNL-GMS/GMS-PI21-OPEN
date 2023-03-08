import { Button } from '@blueprintjs/core';
import React from 'react';

export interface StageExpansionButtonProps {
  readonly isExpanded: boolean;
  readonly disabled: boolean;
  readonly stageName: string;
  readonly toggle: () => void;
}

// eslint-disable-next-line react/function-component-definition
const StageExpansionButtonComponent: React.FunctionComponent<StageExpansionButtonProps> = (
  props: StageExpansionButtonProps
) => {
  const { disabled, stageName, isExpanded, toggle } = props;
  return !disabled ? (
    <Button
      key={stageName}
      className="stage-row__expand-button"
      icon={isExpanded ? 'small-minus' : 'small-plus'}
      onClick={toggle}
      disabled={disabled}
    />
  ) : null;
};

export const StageExpansionButton: React.FunctionComponent<StageExpansionButtonProps> = React.memo(
  StageExpansionButtonComponent
);
