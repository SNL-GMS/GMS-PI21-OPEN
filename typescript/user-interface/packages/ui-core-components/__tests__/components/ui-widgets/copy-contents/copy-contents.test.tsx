import { cleanup, fireEvent, getByText, render } from '@testing-library/react';
import React from 'react';

import { CopyContents } from '../../../../src/ts/components/ui-widgets/copy-contents/copy-contents';

jest.mock('react-toastify', () => {
  const actual = jest.requireActual('react-toastify');

  // do this in here so that it is defined early enough
  Object.defineProperty((global as any).navigator, 'clipboard', {
    value: {
      writeText: jest.fn()
    }
  });
  // this is a no-op. We just use this mock to do some super hoisting so we can mock things early enough
  return actual;
});

describe('CopyContents', () => {
  beforeEach(() => {
    jest.resetAllMocks();
  });
  afterEach(() => {
    cleanup();
  });
  describe('with all the optional props', () => {
    const displayText = 'Click me';
    const copyText = 'This is what to actually copy';
    let container1;
    beforeEach(() => {
      container1 = render(
        <CopyContents
          className="test-class"
          clipboardText={copyText}
          tooltipClassName="test-class__tooltip-wrapper"
          tooltipLabel="The Tooltip"
        >
          {displayText}
        </CopyContents>
      ).container;
    });
    it('copies the clipboardText to the clipboard when clicked', () => {
      const writeTextSpy = jest.spyOn((global as any).navigator.clipboard, 'writeText');
      fireEvent(
        getByText(container1, displayText),
        new MouseEvent('click', {
          bubbles: true,
          cancelable: true
        })
      );
      expect(writeTextSpy).toHaveBeenCalledWith(copyText);
    });
    it('matches a snapshot', () => {
      expect(container1).toMatchSnapshot();
    });
  });

  describe('without all the optional props', () => {
    const displayText = 'The text to copy';
    let container2;
    beforeEach(() => {
      container2 = render(
        <CopyContents className="test-class" tooltipClassName="test-class__tooltip-wrapper">
          {displayText}
        </CopyContents>
      ).container;
    });
    it('matches a snapshot', () => {
      expect(container2).toMatchSnapshot();
    });
    it('copies the contents to the clipboard when clicked if not given clipboardText', () => {
      const writeTextSpy = jest.spyOn((global as any).navigator.clipboard, 'writeText');
      fireEvent(
        getByText(container2, displayText),
        new MouseEvent('click', {
          bubbles: true,
          cancelable: true
        })
      );
      expect(writeTextSpy).toHaveBeenCalledWith(displayText);
    });
  });
});
