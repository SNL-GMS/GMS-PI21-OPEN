import classNames from 'classnames';
import React from 'react';

export interface WorkflowRowLabelProps {
  readonly label: string;
  readonly isActivityRow: boolean;
}

// eslint-disable-next-line react/function-component-definition
export const WorkflowRowLabel: React.FunctionComponent<WorkflowRowLabelProps> = (
  props: WorkflowRowLabelProps
) => {
  const { label, isActivityRow } = props;
  return (
    <div
      key={label}
      data-cy={`workflow-table-label-${label}`}
      className={classNames('workflow-table-label', {
        'workflow-table-label--activity': isActivityRow
      })}
    >
      <div>{label}</div>
    </div>
  );
};
