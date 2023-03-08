import { UploadFile } from '@mui/icons-material';
import { Button } from '@mui/material';
import React from 'react'
import { windowAPI } from '../electron-util';

interface ProcessingStationGroupFilePickerProps {
  className?: string;
}

export const ProcessingStationGroupFilePicker: React.FC<ProcessingStationGroupFilePickerProps> = ({className}: ProcessingStationGroupFilePickerProps) => {
  return <label htmlFor="raised-button-file" className={className ?? 'file-picker'}>
    <Button
      variant="contained" component="span" 
      startIcon={<UploadFile />}
      onClick={async () => {
        const path = await windowAPI.electronAPI.getFilePath();
        await windowAPI.electronAPI.setAppSettings({processingStationGroupFilePath: path});
      }}
    >
      Choose File
    </Button>
  </label>
}
