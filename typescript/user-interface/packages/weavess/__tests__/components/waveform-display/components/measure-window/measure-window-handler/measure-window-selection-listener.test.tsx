/* eslint-disable @typescript-eslint/no-magic-numbers */
/* eslint-disable jsx-a11y/no-static-element-interactions */
/* eslint-disable no-plusplus */
import { render } from '@testing-library/react';
import * as Enzyme from 'enzyme';
import * as React from 'react';

import { MeasureWindowSelectionListener } from '../../../../../../src/ts/components/waveform-display/components/measure-window/measure-window-selection/measure-window-selection-listener';
import {
  actAndWaitForComponentToPaint,
  documentMoveMouse,
  documentReleaseMouse,
  dragElement
} from '../../../../../test-util/test-util';

describe('Measure window selection', () => {
  const toaster = jest.fn();
  const updater = jest.fn();
  const computer = jest.fn();
  const dummyEvent = ({
    preventDefault: jest.fn(),
    shiftKey: false,
    clientX: 50,
    clientY: 50,
    altKey: true,
    stopPropagation: jest.fn(() => true)
  } as unknown) as React.MouseEvent<HTMLDivElement>;

  const modifierKeyDownEvent: KeyboardEvent = new KeyboardEvent('keydown', {
    key: 'Alt',
    code: 'AltLeft',
    altKey: true
  });
  const modifierKeyUpEvent: KeyboardEvent = new KeyboardEvent('keyup', {
    key: 'Alt',
    code: 'AltLeft',
    altKey: true
  });

  const wrapper = Enzyme.mount(
    <MeasureWindowSelectionListener
      displayInterval={{
        startTimeSecs: 0,
        endTimeSecs: 1000
      }}
      offsetSecs={0}
      isMeasureWindowEnabled={jest.fn(() => true)}
      toast={toaster}
      hotKeys={{ amplitudeScale: 'KeyY', drawMeasureWindow: 'Alt' }}
      updateMeasureWindowPanel={updater}
      computeTimeSecsFromMouseXPixels={computer}
    >
      {({ contentRenderer, onMouseDown }) => {
        return (
          <>
            <div id="START" onMouseDown={() => onMouseDown(dummyEvent)} />
            {contentRenderer}
          </>
        );
      }}
    </MeasureWindowSelectionListener>
  );

  beforeAll(async () => {
    const holdAlt = () => {
      document.body.dispatchEvent(modifierKeyDownEvent);
    };
    const releaseAlt = () => {
      document.body.dispatchEvent(modifierKeyUpEvent);
    };
    const callMouseDown = () => {
      const startMouseDown = wrapper.find('#START');
      startMouseDown.simulate('mousedown');
    };
    await actAndWaitForComponentToPaint(wrapper, holdAlt);
    await actAndWaitForComponentToPaint(wrapper, callMouseDown);
    await actAndWaitForComponentToPaint(wrapper, releaseAlt);
  });

  it('should return a contentRenderer that matches a snapshot', () => {
    const { container } = render(
      <MeasureWindowSelectionListener
        displayInterval={{
          startTimeSecs: 0,
          endTimeSecs: 1000
        }}
        offsetSecs={0}
        isMeasureWindowEnabled={jest.fn(() => true)}
        toast={toaster}
        hotKeys={{ amplitudeScale: 'KeyY', drawMeasureWindow: 'Alt' }}
        updateMeasureWindowPanel={updater}
        computeTimeSecsFromMouseXPixels={computer}
      >
        {({ contentRenderer, onMouseDown }) => {
          return (
            <>
              <div id="START" onMouseDown={() => onMouseDown(dummyEvent)} />
              {contentRenderer}
            </>
          );
        }}
      </MeasureWindowSelectionListener>
    );
    expect(container).toMatchSnapshot();
  });

  let numCalls = 0;
  it('calls updater on mouse move', async () => {
    await actAndWaitForComponentToPaint(wrapper, documentMoveMouse);
    await actAndWaitForComponentToPaint(wrapper, documentReleaseMouse);
    expect(updater).toHaveBeenCalledTimes(++numCalls);
  });

  it('should have removed mouseup listener on previous mouse up', async () => {
    await actAndWaitForComponentToPaint(wrapper, documentMoveMouse);
    await actAndWaitForComponentToPaint(wrapper, documentReleaseMouse);
    expect(updater).toHaveBeenCalledTimes(numCalls); // do not increment
  });

  it('calls updater on measure window click', async () => {
    const clickAndDragMeasureWindow = () => {
      dragElement(wrapper, '.measure-window-selection__overlay');
      documentMoveMouse();
    };
    await actAndWaitForComponentToPaint(wrapper, clickAndDragMeasureWindow);
    await actAndWaitForComponentToPaint(wrapper, documentReleaseMouse);
    expect(updater).toHaveBeenCalledTimes(++numCalls);
  });
});
