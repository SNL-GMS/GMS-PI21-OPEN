import { OwnPropsOfControl } from '@jsonforms/core';
import { withJsonFormsControlProps } from '@jsonforms/react';
import { URLInput } from './URLInput';

interface URLInputControlProps extends OwnPropsOfControl {
  data: any;
  handleChange(path: string, value: any): void;
  path: string;
}

const URLInputControl = ({
  data,
  handleChange,
  path,
  uischema,
}: URLInputControlProps) => (
  <URLInput
    updateURL={(newValue: string) => handleChange(path, newValue)}
    data={data}
    label={`${uischema?.label}`}
    testEndpoint={uischema?.options?.testEndpoint}
    description={uischema?.options?.description}
  />
);

export default withJsonFormsControlProps(URLInputControl);
