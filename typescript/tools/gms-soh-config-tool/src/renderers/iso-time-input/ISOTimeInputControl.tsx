import { OwnPropsOfControl } from '@jsonforms/core';
import { withJsonFormsControlProps } from '@jsonforms/react';
import { HelpTextRenderer } from '../../components/HelpTextRenderer';
import { ISOTimeInput } from './ISOTimeInput';

interface ISOTimeInputControlProps extends OwnPropsOfControl {
  data: string;
  handleChange(path: string, value: any): void;
  path: string;
}

const ISOTimeInputControl = ({
  data,
  handleChange,
  path,
  uischema,
}: ISOTimeInputControlProps) => (
  <HelpTextRenderer helpText={uischema?.options?.help}>
    <ISOTimeInput
      update={(newValue: string) => handleChange(path, newValue)}
      data={data}
      label={`${uischema?.label}`}
      canInputBeZero={false}
      options={uischema?.options?.options}
      description={uischema?.options?.description}
    />
  </HelpTextRenderer>
);

export default withJsonFormsControlProps(ISOTimeInputControl);
