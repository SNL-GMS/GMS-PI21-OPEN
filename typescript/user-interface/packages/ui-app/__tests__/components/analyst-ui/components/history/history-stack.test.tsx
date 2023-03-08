import { render } from '@testing-library/react';
import Enzyme from 'enzyme';
import React from 'react';

import { HistoryStack } from '../../../../../src/ts/components/analyst-ui/components/history/history-stack/history-stack';
import type { HistoryStackProps } from '../../../../../src/ts/components/analyst-ui/components/history/history-stack/types';
import { HistoryEntryAction } from '../../../../../src/ts/components/analyst-ui/components/history/history-stack/types';
import * as HistoryTypes from '../../../../../src/ts/components/analyst-ui/components/history/types';
import * as Utils from '../../../../../src/ts/components/analyst-ui/components/history/utils/history-utils';
import { historyList, providerState } from '../../../../__data__/history-data';

/* eslint-disable newline-per-chained-call */
/* eslint-disable @typescript-eslint/unbound-method */
describe('history stack', () => {
  const props: HistoryStackProps = {
    historyActions: new Map([
      [HistoryEntryAction.undo, jest.fn()],
      [HistoryEntryAction.redo, jest.fn()]
    ]),
    isIncluded: jest.fn().mockReturnValue(true)
  };

  providerState.setHistoryActionIntent = jest.fn();
  providerState.undoEventHistoryById = jest.fn();
  providerState.historyActionIntent.isEventMode = true;
  providerState.historyActionIntent.isIncluded = jest.fn().mockReturnValue(true);

  const wrapper = Enzyme.mount(
    <HistoryTypes.HistoryContext.Provider value={providerState}>
      {/* eslint-disable-next-line react/jsx-props-no-spreading */}
      <HistoryStack {...props} />
    </HistoryTypes.HistoryContext.Provider>
  );
  const instance = wrapper.instance() as any;

  test('renders a stack with basic props', () => {
    const { container } = render(
      <HistoryTypes.HistoryContext.Provider value={providerState}>
        {/* eslint-disable-next-line react/jsx-props-no-spreading */}
        <HistoryStack {...props} />
      </HistoryTypes.HistoryContext.Provider>
    );
    expect(container).toMatchSnapshot();
  });

  // generateHistoryEntryDisplayFlags tests

  const undoDisplayFlags = instance.generateHistoryEntryDisplayFlags(historyList[0].changes[0], 0);
  const redoDisplayFlags = instance.generateHistoryEntryDisplayFlags(historyList[1].changes[2], 1);
  const undefinedDisplayFlags = instance.generateHistoryEntryDisplayFlags({
    change: 'wrong data type'
  });
  const historyEntries = wrapper.find('.history-entry');

  test('private function generateHistoryEntryDisplayFlags exists', () => {
    expect(instance.generateHistoryEntryDisplayFlags).toBeDefined();
  });

  test('private function generateHistoryEntryDisplayFlags contains correct undo/redo entry types', () => {
    expect(undefinedDisplayFlags).toBeUndefined();
    expect(undoDisplayFlags).toBeDefined();
    expect(redoDisplayFlags).toBeDefined();
    expect(undoDisplayFlags.entryType).toEqual(HistoryEntryAction.undo);
    expect(redoDisplayFlags.entryType).toEqual(HistoryEntryAction.redo);
  });

  test('private function generateHistoryEntryDisplayFlags matches snapshot', () => {
    expect(undoDisplayFlags).toMatchSnapshot();
  });

  // getHandleMouseEnter tests
  const mouseEnterFn = instance.getHandleMouseEnter(historyList[0]);

  test('getHandleMouseEnter returns a function', () => {
    expect(mouseEnterFn).toBeDefined();
    expect(mouseEnterFn).toBeInstanceOf(Function);
  });

  test('getHandleMouseEnter calls setHistoryActionIntent', () => {
    expect(historyEntries.length).toBeGreaterThan(0);
    historyEntries.forEach((entry, index) => {
      const hoverEvent = entry.simulate('mouseenter');
      mouseEnterFn(hoverEvent);
      expect(providerState.setHistoryActionIntent).toHaveBeenCalledTimes(index + 1);
    });
    expect(providerState.setHistoryActionIntent).toHaveBeenCalledTimes(historyEntries.length);
  });

  // getHandleAction tests with an undo entry
  const actionFn = instance.getHandleMouseEnter(historyList[0]);

  test('getHandleAction calls undoHistoryById for a click on an undo entry', () => {
    expect(historyEntries.length).toBeGreaterThan(0);
    historyEntries.forEach(entry => {
      const clickEvent = entry.simulate('click', { ctrlKey: false });
      actionFn(clickEvent);
      expect(props.historyActions.get(HistoryEntryAction.undo)).toHaveBeenCalled();
    });
    const numberOfUndos = Utils.getNumberOfUndos(historyList);
    expect(props.historyActions.get(HistoryEntryAction.undo)).toHaveBeenCalledTimes(numberOfUndos);
  });

  test('getHandleAction calls undoEventHistoryById for a ctrl + click on an undo entry', () => {
    expect(historyEntries.length).toBeGreaterThan(0);
    historyEntries.forEach(entry => {
      const clickEvent = entry.simulate('click', { ctrlKey: true });
      actionFn(clickEvent);
      expect(providerState.undoEventHistoryById).toHaveBeenCalled();
    });
    const numberOfUndos = Utils.getNumberOfUndos(historyList);
    expect(providerState.undoEventHistoryById).toHaveBeenCalledTimes(numberOfUndos);
  });

  // getGenerateMouseOut
  const mouseOutFn = instance.getGenerateMouseOut();

  test('getGenerateMouseOut calls setHistoryActionIntent with undefined', () => {
    expect(mouseOutFn).toBeDefined();
    mouseOutFn();
    expect(providerState.setHistoryActionIntent).toHaveBeenCalled();
    expect(providerState.setHistoryActionIntent).toHaveBeenCalledWith(undefined);
  });

  // getHandleKeyDown
  const keyDownFn = instance.getHandleKeyDown(historyList[0]);

  test('getHandleKeyDown sets historyActionIntent.isChangeIncluded meta keydown', () => {
    expect(keyDownFn).toBeDefined();
    expect(historyEntries.length).toBeGreaterThan(0);
    providerState.setHistoryActionIntent = jest.fn();
    historyEntries.forEach((entry, index) => {
      const keyDownEvent = entry.simulate('keydown', { ctrlKey: true });
      keyDownFn(keyDownEvent);
      expect(providerState.setHistoryActionIntent).toHaveBeenCalledTimes(index + 1);
    });
  });

  // getHandleKeyup
  const keyUpFn = instance.getHandleKeyUp(historyList[0]);

  test('getHandleKeyUp calls setHistoryActionIntent only if the ctrl/meta key is not depressed', () => {
    expect(keyUpFn).toBeDefined();
    expect(keyUpFn).toBeInstanceOf(Function);
    expect(historyEntries.length).toBeGreaterThan(0);

    providerState.setHistoryActionIntent = jest.fn().mockReturnValue('keyUpEvent');
    historyEntries.forEach((entry, index) => {
      entry.simulate('keyup', { ctrlKey: true, metaKey: false });
      expect(providerState.setHistoryActionIntent).toHaveBeenCalledTimes(index);
      entry.simulate('keyup', { ctrlKey: false, metaKey: false });
      expect(providerState.setHistoryActionIntent).toHaveBeenCalledTimes(index + 1);
    });
  });
});
