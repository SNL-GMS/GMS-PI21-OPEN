import { OwnPropsOfControl } from '@jsonforms/core';
import { withJsonFormsControlProps } from '@jsonforms/react';
import { FilePathInput } from './FilePathInput';

interface FilePathInputControlProps extends OwnPropsOfControl {
  data: any;
  handleChange(path: string, value: any): void;
  path: string;
}

const FilePathInputControl = ({
  data,
  handleChange,
  path,
  uischema,
}: FilePathInputControlProps) => (
  <FilePathInput
    updateFilePath={(newValue: any) => handleChange(path, newValue)}
    data={data}
    label={`${uischema?.label}`}
    description={uischema?.options?.description}
    defaultValue={uischema?.options?.defaultValue}
  />
);

export default withJsonFormsControlProps(FilePathInputControl);
