import { render } from '@testing-library/react';
import { renderHook } from '@testing-library/react-hooks';
import * as React from 'react';

import { useHandleGutterClick } from '../../../src/ts/components/scroll-bar-override/scroll-bar-override';
import { ScrollBarOverride } from '../../../src/ts/ui-core-components';

describe('Scroll Bar Override', () => {
  const mockUndefinedElement = ({
    parentElement: undefined,
    getBoundingClientRect: jest.fn(() => ({ width: 100 }))
  } as undefined) as Element;
  const mockTargetElement = ({
    parentElement: {
      scrollTo: jest.fn(),
      scrollWidth: 1000,
      className: 'test-scroll-bar-parent'
    },
    getBoundingClientRect: jest.fn(() => ({ width: 100 }))
  } as undefined) as Element;
  describe('component', () => {
    it('matches a snapshot', () => {
      const { container } = render(
        <ScrollBarOverride orientation="x" scrollLeft={0} targetElement={mockTargetElement} />
      );
      expect(container).toMatchSnapshot();
    });
    it('matches a snapshot with no parent', () => {
      const { container } = render(
        <ScrollBarOverride orientation="x" scrollLeft={0} targetElement={mockUndefinedElement} />
      );
      expect(container).toMatchSnapshot();
    });
  });
  describe('useHandleGutterClick', () => {
    it("returns a function that scrolls the target element's parent", () => {
      const mockScrollBarRef = ({
        current: {
          getBoundingClientRect: jest.fn(() => ({ width: 10 })),
          style: { transform: '' }
        }
      } as undefined) as React.MutableRefObject<HTMLElement>;
      const mockScrollBarGutterRef = ({
        current: {
          getBoundingClientRect: jest.fn(() => ({ width: 100 }))
        }
      } as undefined) as React.MutableRefObject<HTMLElement>;
      const mockBoundX = jest.fn(arg => arg);
      const { result } = renderHook(() =>
        useHandleGutterClick(
          mockScrollBarRef,
          mockScrollBarGutterRef,
          mockBoundX,
          mockTargetElement,
          jest.fn()
        )
      );
      result.current({ clientX: 0, clientY: 0 } as any);
      // eslint-disable-next-line @typescript-eslint/unbound-method
      expect(mockTargetElement.parentElement.scrollTo).toHaveBeenCalled();
    });
  });
});
