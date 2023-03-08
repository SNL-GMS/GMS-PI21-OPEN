/* eslint-disable react/jsx-no-constructed-context-values */
import { getStore } from '@gms/ui-state';
import { render } from '@testing-library/react';
import * as React from 'react';
import { Provider } from 'react-redux';

import { signalDetectionsColumnsToDisplay } from '../../../../../src/ts/components/analyst-ui/components/signal-detections/table/signal-detections-table-utils';
import {
  onChangeHandler,
  SignalDetectionsToolbar
} from '../../../../../src/ts/components/analyst-ui/components/signal-detections/toolbar/signal-detections-toolbar';
import { BaseDisplayContext } from '../../../../../src/ts/components/common-ui/components/base-display/base-display-context';

describe('Signal Detection Toolbar', () => {
  test('can be mounted', () => {
    expect(SignalDetectionsToolbar).toBeDefined();
  });

  test('should match snapshot', () => {
    const { container } = render(
      <Provider store={getStore()}>
        <BaseDisplayContext.Provider
          value={{
            glContainer: {} as any,
            widthPx: 100,
            heightPx: 100
          }}
        >
          <SignalDetectionsToolbar
            setSelectedSDColumnsToDisplay={jest.fn()}
            selectedSDColumnsToDisplay={signalDetectionsColumnsToDisplay}
          />
        </BaseDisplayContext.Provider>
      </Provider>
    );
    expect(container).toMatchSnapshot();
  });

  test('checkbox list onChangeHandler with configuration arguments', () => {
    const dispatch = jest.fn();
    const eventObject = {
      syncWaveform: false,
      signalDetectionBeforeInterval: true,
      signalDetectionAfterInterval: true,
      signalDetectionAssociatedToOpenEvent: true,
      signalDetectionAssociatedToCompletedEvent: true,
      signalDetectionAssociatedToOtherEvent: true,
      signalDetectionUnassociated: false
    };
    onChangeHandler(dispatch)(eventObject);
    expect(dispatch).toHaveBeenCalledTimes(1);
  });
});
