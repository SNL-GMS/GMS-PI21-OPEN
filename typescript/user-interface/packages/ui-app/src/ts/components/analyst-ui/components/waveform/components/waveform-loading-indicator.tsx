import { Intent, Spinner, SpinnerSize } from '@blueprintjs/core';
import { useAppSelector } from '@gms/ui-state';
import * as React from 'react';

// Listens to Waveform Client waveform loading state in redux
// eslint-disable-next-line react/function-component-definition
export const WaveformLoadingIndicator: React.FC = () => {
  const waveformClientState = useAppSelector(state => state.app.waveform.loadingState);
  return (
    <div className="loading-indicator__container" data-cy="waveform-loading-indicator">
      {waveformClientState.isLoading && (
        <Spinner
          intent={Intent.PRIMARY}
          size={SpinnerSize.SMALL}
          value={waveformClientState.percent}
        />
      )}
      {waveformClientState.isLoading && (
        <span className="loading-indicator__description"> {waveformClientState.description}</span>
      )}
    </div>
  );
};
