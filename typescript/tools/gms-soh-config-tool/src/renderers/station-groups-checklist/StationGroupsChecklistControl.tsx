import { OwnPropsOfControl } from '@jsonforms/core';
import { withJsonFormsControlProps } from '@jsonforms/react';
import { StationGroupsChecklist } from './StationGroupsChecklist';

interface StationGroupsChecklistControlProps extends OwnPropsOfControl {
  data: any;
  handleChange(path: string, value: any): void;
  path: string;
}

const StationGroupsChecklistControl = ({
  data,
  handleChange,
  path,
  uischema,
}: StationGroupsChecklistControlProps) => (
  <StationGroupsChecklist
    formData={data}
    label={`${uischema?.label}`}
    description={uischema?.options?.description}
  />
);

export default withJsonFormsControlProps(StationGroupsChecklistControl);
