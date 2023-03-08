import React from 'react'
import { FileHandler } from './FileHandler';

interface FileUploadProps {
  className?: string;
  updateData: (newData: unknown) => void;
}

const fileReader: FileReader = new FileReader();

const buildOnLoadEnd = (updateData: (newData: unknown) => void, file: File) => {
  return () => {
    if (typeof fileReader.result !== 'string') {
      console.warn(`File ${file.name} data was not parsed as a string—it was parsed as an array buffer. This will likely cause errors with this data.`)
      updateData(fileReader.result);
    }
    updateData(JSON.parse(fileReader.result as string));
  }
}

export const FileUpload: React.FC<FileUploadProps> = ({className, updateData}: FileUploadProps) => {
  return <FileHandler 
    className={className}
    updateData={updateData}
    buildOnLoadEndHandler={buildOnLoadEnd}
  />
}

FileUpload.defaultProps = {
  className: 'file-upload'
}
