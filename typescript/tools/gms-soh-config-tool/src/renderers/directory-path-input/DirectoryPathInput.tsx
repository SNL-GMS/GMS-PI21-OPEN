import { FolderOpen } from '@mui/icons-material';
import React from 'react';
import { InputWithButton } from '../../components/InputWithButton';
import { windowAPI } from '../../electron-util';

interface DirectoryPathInputProps {
  className?: string;
  updateDirPath: (filePath: string) => void;
  data: any;
  label?: string;
  description?: string;
}

export const DirectoryPathInput: React.FC<DirectoryPathInputProps> = ({
  className,
  updateDirPath,
  label,
  data,
  description,
}: DirectoryPathInputProps) => {
  const onClick = React.useCallback(async () => {
    const path = await windowAPI.electronAPI.getDirPath();
    if (path) {
      updateDirPath(path);
    }
  }, [updateDirPath]);
  return (
    <InputWithButton
      id='#/properties/file-upload'
      className={className ?? 'directory-path-input'}
      label={label}
      startIcon={<FolderOpen />}
      data={data}
      onClick={onClick}
      onInputClick={onClick}
      buttonText='Choose directory'
      defaultValue='Choose a directory'
      description={description}
      isEditable={false}
    />
  );
};
