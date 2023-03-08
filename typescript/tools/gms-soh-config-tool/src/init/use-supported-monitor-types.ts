import produce, { Draft } from 'immer';
import { isEqual } from 'lodash';
import React from 'react';
import { allMonitorTypeNames, MonitorType } from '../coi-types/monitor-types';
import { windowAPI } from '../electron-util';
import { setError } from '../state/app-settings-slice';
import { useAppDispatch } from '../state/react-redux-hooks';
import { LoadingState } from '../state/state';
import { useOnce } from '../util/custom-hooks';

export const useSupportedMonitorTypes = (
  data: any,
  setData: React.Dispatch<any>,
  setLoadingState: React.Dispatch<React.SetStateAction<LoadingState>>
) => {
  const dispatch = useAppDispatch();
  const [path, setPath] = React.useState(undefined);
  const [supportedMonitorTypes, setSupportedMonitorTypes] = React.useState<
    MonitorType[] | undefined
  >(undefined);
  useOnce(() => {
    const getPath = async () => {
      const p = await windowAPI.electronAPI.getAppSetting(
        'supportedMonitorTypesFilePath'
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
        setSupportedMonitorTypes(fileData);
      }
    };
    updateData();
  }, [path, setLoadingState]);

  if (!isEqual(supportedMonitorTypes, data.supportedMonitorTypes)) {
    supportedMonitorTypes?.forEach((monitorType) => {
      if (!allMonitorTypeNames.includes(monitorType)) {
        const errorMsg = `Invalid monitor type ${monitorType} found in supported monitor types app configuration file. Please check your app configuration, and make sure all monitor types configured are supported.`;
        console.error(errorMsg);
        dispatch(setError(errorMsg));
      }
    });
    setData(
      produce(data, (draft: Draft<any>) => {
        draft.supportedMonitorTypes = supportedMonitorTypes;
      })
    );
  }
  return supportedMonitorTypes;
};
