import { OwnPropsOfControl } from '@jsonforms/core';
import { withJsonFormsControlProps } from '@jsonforms/react';
import { StationDurations } from './StationDurations';

interface StationDurationsControlProps extends OwnPropsOfControl {
  data: any;
  handleChange(path: string, value: any): void;
  path: string;
}

const StationDurationsControl = ({
  data,
  handleChange,
  path,
  uischema,
}: StationDurationsControlProps) => (
  <StationDurations
    label={`${uischema?.label}`}
    description={uischema?.options?.description}
    options={uischema?.options?.options}
  />
);

export default withJsonFormsControlProps(StationDurationsControl);
