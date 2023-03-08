import { UploadFile } from '@mui/icons-material';
import React from 'react';
import { InputWithButton } from '../../components/InputWithButton';
import { windowAPI } from '../../electron-util';

interface FilePathInputProps {
  className?: string;
  updateFilePath: (filePath: string) => void;
  data: any;
  label?: string;
  description?: string;
  defaultValue?: string;
}

export const FilePathInput: React.FC<FilePathInputProps> = ({
  className,
  updateFilePath,
  label,
  data,
  description,
  defaultValue,
}: FilePathInputProps) => {
  const onClick = React.useCallback(async () => {
    const path = await windowAPI.electronAPI.getFilePath();
    if (path) {
      updateFilePath(path);
    }
  }, [updateFilePath]);
  return (
    <InputWithButton
      id='#/properties/file-upload'
      className={className ?? 'file-path-input'}
      label={label}
      startIcon={<UploadFile />}
      data={data}
      onClick={onClick}
      onInputClick={onClick}
      buttonText='Choose a file'
      defaultValue={defaultValue ?? 'Choose a file'}
      description={description}
      isEditable={false}
    />
  );
};
