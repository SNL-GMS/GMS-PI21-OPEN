import type React from 'react';

import * as SelectionUtils from '../../src/ts/ui-util/selection-util';

describe('Selection Utils', () => {
  const theSelection = ['Station_1', 'Station_2', 'Station_5'];
  const theList = [];
  // eslint-disable-next-line @typescript-eslint/no-magic-numbers
  for (let i = 0; i < 10; i += 1) {
    theList.push({ id: `Station_${i}` });
  }

  it('gets bounding (first and last) indices from a selection', () => {
    const [firstIndex, lastIndex] = SelectionUtils.getBoundingIndices(theSelection, theList);
    expect(firstIndex).toEqual(1);
    // eslint-disable-next-line @typescript-eslint/no-magic-numbers
    expect(lastIndex).toEqual(5);
  });

  it('can determine if an index is in selection', () => {
    const isInSelection = SelectionUtils.isInSelection(1, theSelection, theList);
    expect(isInSelection).toBeTruthy();
  });

  it('can determine that an index is not in selection', () => {
    // eslint-disable-next-line @typescript-eslint/no-magic-numbers
    const isInSelection = SelectionUtils.isInSelection(9, theSelection, theList);
    expect(isInSelection).toBeFalsy();
  });

  it('replaces a selection when no modifier keys are held', () => {
    const e = {
      metaKey: false,
      shiftKey: false
    };
    const updatedSelection = SelectionUtils.getSelectionFromClick(
      e as React.MouseEvent,
      theSelection,
      0,
      theList
    );
    expect(updatedSelection).toHaveLength(1);
  });

  it('adds to a selection when the meta key is held and target is new', () => {
    const e = {
      metaKey: true,
      shiftKey: false
    };
    const updatedSelection = SelectionUtils.getSelectionFromClick(
      e as React.MouseEvent,
      theSelection,
      0,
      theList
    );
    expect(updatedSelection).toHaveLength(4);
  });

  it('removes from a selection when the meta key is held and target is already selected', () => {
    const e = {
      metaKey: true,
      shiftKey: false
    };
    const updatedSelection = SelectionUtils.getSelectionFromClick(
      e as React.MouseEvent,
      theSelection,
      1,
      theList
    );
    expect(updatedSelection).toHaveLength(2);
  });

  it('adds to a selection when the shift key is held and target is new', () => {
    const e = {
      metaKey: false,
      shiftKey: true
    };
    const updatedSelection = SelectionUtils.getSelectionFromClick(
      e as React.MouseEvent,
      theSelection,
      0,
      theList
    );
    expect(updatedSelection).toHaveLength(4);
  });

  it('removes subsequent entries from a selection when the shift key is held and target is selected', () => {
    const e = {
      metaKey: false,
      shiftKey: true
    };
    const updatedSelection = SelectionUtils.getSelectionFromClick(
      e as React.MouseEvent,
      theSelection,
      2,
      theList
    );
    expect(updatedSelection).toHaveLength(2);
  });
});
