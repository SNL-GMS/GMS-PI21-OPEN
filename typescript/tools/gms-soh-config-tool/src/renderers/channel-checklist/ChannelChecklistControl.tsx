import { OwnPropsOfControl } from '@jsonforms/core';
import { withJsonFormsControlProps } from '@jsonforms/react';
import { ChannelChecklist } from './ChannelChecklist';

interface ChannelChecklistControlProps extends OwnPropsOfControl {
  data: any;
  handleChange(path: string, value: any): void;
  path: string;
}

const ChannelChecklistControl = ({
  data,
  handleChange,
  path,
  uischema,
}: ChannelChecklistControlProps) => (
  <ChannelChecklist
    updateChannelChecklist={(newValue: any) => handleChange(path, newValue)}
    formData={data}
    label={`${uischema?.label}`}
    description={uischema?.options?.description}
  />
);

export default withJsonFormsControlProps(ChannelChecklistControl);
