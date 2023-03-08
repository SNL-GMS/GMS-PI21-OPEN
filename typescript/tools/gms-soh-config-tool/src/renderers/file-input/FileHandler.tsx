import React, { FormEvent } from 'react'
import { Button } from '@mui/material';

interface FileHandlerProps {
  className?: string;
  updateData: (newData: unknown) => void;
  buildOnLoadEndHandler: (updateData: (newData: unknown) => void, file: File) => () => void;
}

const fileReader: FileReader = new FileReader();

export const FileHandler: React.FC<FileHandlerProps> = ({className, updateData, buildOnLoadEndHandler}: FileHandlerProps) => {
  return <div id={'#/properties/file-upload'} className={className}>
    <input
      accept=".json"
      className={className}
      style={{ display: 'none' }}
      id="raised-button-file"
      multiple
      type="file"
      onInput={(e: FormEvent<HTMLInputElement>) => {
        const fileData = e.currentTarget.files ? e.currentTarget.files[0] : undefined;
        if (!fileData) {
          return;
        }
        fileReader.onloadend = buildOnLoadEndHandler(updateData, fileData);
        fileReader.readAsText(fileData);
      }}
    />
    <label htmlFor="raised-button-file">
      <Button variant="contained" component="span">
        Upload
      </Button>
    </label> 
  </div>;
}

FileHandler.defaultProps = {
  className: 'file-upload'
}
