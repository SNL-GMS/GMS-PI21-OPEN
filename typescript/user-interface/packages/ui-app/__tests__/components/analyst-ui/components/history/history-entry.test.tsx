import { render } from '@testing-library/react';
import React from 'react';

import {
  GenericHistoryEntry,
  makeKeyHandler,
  makeMouseHandler
} from '../../../../../src/ts/components/analyst-ui/components/history/history-stack/history-entry';
import type { GenericHistoryEntryProps } from '../../../../../src/ts/components/analyst-ui/components/history/history-stack/types';
import { HistoryEntryAction } from '../../../../../src/ts/components/analyst-ui/components/history/history-stack/types';

describe('history entry', () => {
  const history: GenericHistoryEntryProps = {
    message: 'test',
    entryType: HistoryEntryAction.undo,
    isChild: false,
    isAssociated: true,
    isOrphaned: false,
    isInConflict: false,
    isEventReset: false,
    isAffected: false,
    handleMouseOut: undefined,
    handleAction: undefined,
    handleKeyDown: undefined,
    handleKeyUp: undefined,
    handleMouseEnter: undefined
  };
  test('renders correctly 01', () => {
    // eslint-disable-next-line react/jsx-props-no-spreading
    const { container } = render(<GenericHistoryEntry {...history} />);
    expect(container).toMatchSnapshot();
  });
  test('renders correctly 02', () => {
    const { container } = render(
      <GenericHistoryEntry
        // eslint-disable-next-line react/jsx-props-no-spreading
        {...history}
        isAssociated={false}
        isInConflict
        entryType={HistoryEntryAction.redo}
        isAffected
        isEventReset
        isOrphaned
      />
    );
    expect(container).toMatchSnapshot();
  });
  test('mouse in and out handlers work', () => {
    const handler = jest.fn();
    const handledHandler = makeMouseHandler(handler, true);
    const fauxEvent = {
      currentTarget: {
        focus: jest.fn()
      }
    };
    handledHandler(fauxEvent);
    expect(fauxEvent.currentTarget.focus).toHaveBeenCalled();
  });
  test('key handlers work', () => {
    const handler = jest.fn();
    const handledHandler = makeKeyHandler(handler);
    const fauxEvent = {
      currentTarget: {
        focus: jest.fn()
      }
    };
    handledHandler(fauxEvent);
    expect(handler).toHaveBeenCalled();
  });
});
