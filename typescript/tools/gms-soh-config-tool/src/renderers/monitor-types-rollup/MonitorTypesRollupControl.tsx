import { OwnPropsOfControl } from '@jsonforms/core';
import { withJsonFormsControlProps } from '@jsonforms/react';
import { MonitorTypesRollup } from './MonitorTypesRollup';

interface MonitorTypesRollupControlProps extends OwnPropsOfControl {
  data: any;
  handleChange(path: string, value: any): void;
  path: string;
}

const MonitorTypesRollupControl = ({
  data,
  handleChange,
  path,
  uischema,
}: MonitorTypesRollupControlProps) => (
  <MonitorTypesRollup
    label={`${uischema?.label}`}
    description={uischema?.options?.description}
  />
);

export default withJsonFormsControlProps(MonitorTypesRollupControl);
