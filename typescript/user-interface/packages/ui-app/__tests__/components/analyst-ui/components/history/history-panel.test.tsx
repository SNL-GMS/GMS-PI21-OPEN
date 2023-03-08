import { render } from '@testing-library/react';
import Enzyme from 'enzyme';
import React from 'react';

import {
  getHandleRedoMouseEnter,
  getHandleUndoMouseEnter,
  HistoryPanel
} from '../../../../../src/ts/components/analyst-ui/components/history/history-panel';
import * as HistoryTypes from '../../../../../src/ts/components/analyst-ui/components/history/types';
import { historyList, providerState } from '../../../../__data__/history-data';

describe('history panel', () => {
  const history: HistoryTypes.HistoryPanelProps = {
    nonIdealState: undefined
  };

  test('has exposed function', () => {
    expect(HistoryPanel).toBeDefined();
    expect(getHandleUndoMouseEnter).toBeDefined();
  });

  test('renders non-ideal-state', () => {
    const nonIdealDiv = <div>I&apos;m a non-ideal div, duh</div>;
    const { container } = render(
      <HistoryTypes.HistoryContext.Provider value={providerState}>
        <HistoryPanel
          // eslint-disable-next-line react/jsx-props-no-spreading
          {...history}
          nonIdealState={{
            loadingEvent: nonIdealDiv,
            loadingHistory: undefined
          }}
        />
      </HistoryTypes.HistoryContext.Provider>
    );
    expect(container).toMatchSnapshot();
  });

  providerState.historyList = historyList;
  const wrapper = Enzyme.mount(
    <HistoryTypes.HistoryContext.Provider value={providerState}>
      {/* eslint-disable-next-line react/jsx-props-no-spreading */}
      <HistoryPanel {...history} />
    </HistoryTypes.HistoryContext.Provider>
  );

  test('renders ideal-state', () => {
    const { container } = render(
      <HistoryTypes.HistoryContext.Provider value={providerState}>
        {/* eslint-disable-next-line react/jsx-props-no-spreading */}
        <HistoryPanel {...history} />
      </HistoryTypes.HistoryContext.Provider>
    );
    expect(container).toMatchSnapshot();
  });

  test('has getHandleUndoMouseEnter function', () => {
    providerState.historyList = historyList;
    const mockFunc = jest.fn();
    mockFunc.mockReturnValue(true);
    const returnedFunc = getHandleUndoMouseEnter(providerState, mockFunc);
    expect(returnedFunc).toBeInstanceOf(Function);
    returnedFunc();
    // eslint-disable-next-line @typescript-eslint/unbound-method
    expect(providerState.setHistoryActionIntent).toHaveBeenCalled();
    expect(mockFunc).toHaveBeenCalled();
  });

  test('has getHandleRedoMouseEnter function', () => {
    providerState.historyList = historyList;
    const mockFunc = jest.fn();
    mockFunc.mockReturnValue(true);
    const returnedFunc = getHandleRedoMouseEnter(providerState);
    expect(returnedFunc).toBeInstanceOf(Function);
    returnedFunc();
    // eslint-disable-next-line @typescript-eslint/unbound-method
    expect(providerState.setHistoryActionIntent).toHaveBeenCalled();
  });

  test('undo button click triggers undo call', () => {
    const undoButton = wrapper.find('button[title="Undo last action"]');
    undoButton.simulate('click');
    // eslint-disable-next-line @typescript-eslint/unbound-method
    expect(providerState.undoHistory).toHaveBeenCalledWith(1);
  });

  test('redo button click triggers redo call', () => {
    const redoButton = wrapper.find('button[title="Redo last undone action"]');
    redoButton.simulate('click');
    // eslint-disable-next-line @typescript-eslint/unbound-method
    expect(providerState.redoHistory).toHaveBeenCalledWith(1);
  });
});
