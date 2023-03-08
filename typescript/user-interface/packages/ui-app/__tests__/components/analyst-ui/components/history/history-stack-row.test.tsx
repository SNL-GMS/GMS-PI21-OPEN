import { render } from '@testing-library/react';
import React from 'react';

import type { HistoryStackRowProps } from '../../../../../src/ts/components/analyst-ui/components/history/history-stack/history-stack-row';
import { HistoryStackRow } from '../../../../../src/ts/components/analyst-ui/components/history/history-stack/history-stack-row';

describe('history stack row', () => {
  const history: HistoryStackRowProps = {
    isFirstRow: true,
    areUndoRedoAdjacent: false,
    redoTarget: true,
    undoTarget: false,
    areUndoRedoJoined: true
  };
  const child = <div />;
  test('renders correctly', () => {
    // eslint-disable-next-line react/jsx-props-no-spreading
    const { container } = render(<HistoryStackRow {...history}>{child}</HistoryStackRow>);
    expect(container).toMatchSnapshot();
  });
});
