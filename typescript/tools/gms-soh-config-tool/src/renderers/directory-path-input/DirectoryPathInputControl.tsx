import { OwnPropsOfControl } from '@jsonforms/core';
import { withJsonFormsControlProps } from '@jsonforms/react';
import { DirectoryPathInput } from './DirectoryPathInput';

interface DirectoryPathInputControlProps extends OwnPropsOfControl {
  data: any;
  handleChange(path: string, value: any): void;
  path: string;
}

const DirectoryPathInputControl = ({ data, handleChange, path, uischema }: DirectoryPathInputControlProps) => (
  <DirectoryPathInput
    updateDirPath={(newValue: any) => handleChange(path, newValue)}
    data={data}
    label={`${uischema?.label}`}
    description={uischema?.options?.description}
  />
);

export default withJsonFormsControlProps(DirectoryPathInputControl);
