import React from 'react';
import type { LoadingState } from '../state/state';
import { useConfigFromDirectory } from '../util/custom-hooks';

export const useDataframeReceiver = (
  setData: React.Dispatch<any>,
  setLoadingState: React.Dispatch<React.SetStateAction<LoadingState>>
) => {
  useConfigFromDirectory(
    'dataframe-receiver.channel-lookup',
    setData,
    setLoadingState
  );
};
