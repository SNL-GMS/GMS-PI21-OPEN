import { withJsonFormsControlProps } from '@jsonforms/react';
import { FileUpload } from './FileUpload';

interface FileUploadControlProps {
  data: any;
  handleChange(path: string, value: any): void;
  path: string;
}

const FileUploadControl = ({ data, handleChange, path }: FileUploadControlProps) => (
  <FileUpload
    updateData={(newValue: any) => handleChange(path, newValue)}
  />
);

export default withJsonFormsControlProps(FileUploadControl);
