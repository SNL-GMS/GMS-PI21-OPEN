import { OwnPropsOfControl } from '@jsonforms/core';
import { withJsonFormsControlProps } from '@jsonforms/react';
import { MarkdownRenderer } from './MarkdownRenderer';

interface MarkdownControlProps extends OwnPropsOfControl {
  data: any;
  path: string;
}

const MarkdownControl = ({ data, path, uischema }: MarkdownControlProps) => (
  <MarkdownRenderer
    label={`${uischema?.label}`}
    description={uischema?.options?.description}
  />
);

export default withJsonFormsControlProps(MarkdownControl);
