import React from 'react';
import type { LoadingState } from '../state/state';
import {
  isProcessingStationGroups,
  ProcessingStationGroups,
} from '../coi-types/processing-types';
import { windowAPI } from '../electron-util';
import { useOnce } from '../util/custom-hooks';

export const useProcessingStationGroups = (
  setLoadingState: React.Dispatch<React.SetStateAction<LoadingState>>
) => {
  const [path, setPath] = React.useState(undefined);
  const [processingStationGroupData, setProcessingStationGroupData] =
    React.useState<ProcessingStationGroups | undefined>(undefined);
  useOnce(() => {
    const getPath = async () => {
      const p = await windowAPI.electronAPI.getAppSetting(
        'processingStationGroupFilePath'
      );
      if (p !== path) {
        setPath(p);
      }
    };
    getPath();
  });
  React.useEffect(() => {
    const updateData = async () => {
      if (path) {
        setLoadingState(
          (prev: LoadingState): LoadingState => ({
            numRequested: prev.numRequested + 1,
            numComplete: prev.numComplete,
          })
        );
        const fileData = await windowAPI.electronAPI.loadFile(path);
        setLoadingState(
          (prev: LoadingState): LoadingState => ({
            numRequested: prev.numRequested,
            numComplete: prev.numComplete + 1,
          })
        );
        setProcessingStationGroupData(fileData);
      }
    };
    updateData();
  }, [path, setLoadingState]);
  return processingStationGroupData;
};

export const useStationGroups = (
  processingStationGroupData: ProcessingStationGroups
): any[] => {
  return React.useMemo(() => {
    if (!processingStationGroupData) return [];
    if (!isProcessingStationGroups(processingStationGroupData)) {
      console.error('Bad processing station group');
      return [];
    }
    return processingStationGroupData.map((group: any) => {
      return group.name;
    });
  }, [processingStationGroupData]);
};
