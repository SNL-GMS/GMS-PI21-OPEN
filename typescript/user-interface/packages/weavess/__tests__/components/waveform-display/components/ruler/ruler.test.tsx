/* eslint-disable react/jsx-props-no-spreading */
import { render } from '@testing-library/react';
import * as React from 'react';

import { Ruler } from '../../../../../src/ts/components/waveform-display/components/ruler/ruler';
import type { RulerProps } from '../../../../../src/ts/components/waveform-display/components/ruler/types';

const props: RulerProps = {
  isActive: true,
  initialPoint: { x: 5, y: 10 },
  computeTimeSecsForMouseXFractionalPosition: jest.fn(() => 100),
  containerDimensions: {
    viewport: {
      clientHeight: 100,
      clientWidth: 100,
      scrollWidth: 100,
      scrollLeft: 100,
      scrollTop: 100,
      scrollHeight: 100
    },
    viewportContentContainer: {
      clientWidth: 100
    },
    canvas: {
      rect: {
        bottom: 100,
        height: 100,
        left: 100,
        right: 100,
        top: 100,
        width: 100,
        x: 1,
        y: 10,
        toJSON: jest.fn(() => 'test')
      },
      clientWidth: 100,
      offsetHeight: 100,
      offsetWidth: 100
    }
  },
  onRulerMouseUp: jest.fn()
};

jest.mock('../../../../../src/ts/util/custom-hooks', () => {
  return {
    useFollowMouse: () => 1
  };
});

jest.mock('@gms/ui-util', () => {
  return {
    HotkeyListener: {
      ModifierHotKeys: {
        CONTROL: 'ctl',
        ALT: 'alt',
        SHIFT: 'shift',
        META: 'meta'
      },
      useGlobalHotkeyListener: jest.fn(() => false),
      isKeyDown: jest.fn(() => false)
    }
  };
});

describe('Weavess Ruler Renderer', () => {
  it('to be defined', () => {
    expect(Ruler).toBeDefined();
  });

  it('renders', () => {
    const { container } = render(<Ruler {...props} />);
    expect(container).toMatchSnapshot();
  });
});
