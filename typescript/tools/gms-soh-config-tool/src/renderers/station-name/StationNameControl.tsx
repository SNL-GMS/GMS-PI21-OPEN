import { OwnPropsOfControl } from '@jsonforms/core';
import { withJsonFormsControlProps } from '@jsonforms/react';
import { StationNameInput } from './StationNameInput';

interface StationNameInputControlProps extends OwnPropsOfControl {
  data: any;
  handleChange(path: string, value: any): void;
  path: string;
}

const StationNameInputControl = ({
  data,
  handleChange,
  path,
  uischema,
}: StationNameInputControlProps) => (
  <StationNameInput
    updateStationName={(newValue: any) => handleChange(path, newValue)}
    formData={data}
    label={`${uischema?.label}`}
    description={uischema?.options?.description}
  />
);

export default withJsonFormsControlProps(StationNameInputControl);
