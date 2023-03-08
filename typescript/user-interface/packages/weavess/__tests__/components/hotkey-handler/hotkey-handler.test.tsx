import { getByRole, render } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import * as React from 'react';

import {
  HotkeyHandler,
  PAN_RATIO,
  ZOOM_IN_RATIO,
  ZOOM_OUT_RATIO,
  ZOOM_TARGET_FRACTION
} from '../../../src/ts/components/hotkey-handler';

const panRatio = 0.5;
const zoomInRatio = -0.25;
const zoomOutRatio = 0.5;

const fireKeyEvent = async (key: string, options?: { zIn?: number; zOut?: number; p?: number }) => {
  const mockPan = jest.fn();
  const mockZoom = jest.fn();
  const { container } = render(
    <HotkeyHandler
      pan={mockPan}
      panRatio={options?.p}
      zoomInRatio={options?.zIn}
      zoomOutRatio={options?.zOut}
      zoomByPercentageToPoint={mockZoom}
    >
      hotkey handler contents
    </HotkeyHandler>
  );
  const hotkeyHandler = getByRole(container, 'tab');
  hotkeyHandler.focus();
  await userEvent.keyboard(key);
  return { mockPan, mockZoom };
};

describe('HotkeyHandler', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });
  it('calls pan with provided ratio when KeyA is pressed', async () => {
    const { mockPan } = await fireKeyEvent('a', { p: panRatio });
    expect(mockPan).toHaveBeenCalledWith(-1 * panRatio);
  });
  it('calls pan when KeyD is pressed', async () => {
    const { mockPan } = await fireKeyEvent('d', { p: panRatio });
    expect(mockPan).toHaveBeenCalledWith(panRatio);
  });
  it('calls pan with default ratio when KeyA is pressed and no ratio is provided', async () => {
    const { mockPan } = await fireKeyEvent('a');
    expect(mockPan).toHaveBeenCalledWith(-1 * PAN_RATIO);
  });
  it('calls pan when KeyD is pressed and no ratio is provided', async () => {
    const { mockPan } = await fireKeyEvent('d');
    expect(mockPan).toHaveBeenCalledWith(PAN_RATIO);
  });
  it('calls zoom in with provided ratio in when KeyW is pressed', async () => {
    const { mockZoom } = await fireKeyEvent('w', { zIn: zoomInRatio });
    expect(mockZoom).toHaveBeenCalledWith(zoomInRatio, ZOOM_TARGET_FRACTION);
  });
  it('calls zoom out when KeyS is pressed', async () => {
    const { mockZoom } = await fireKeyEvent('s', { zOut: zoomOutRatio });
    expect(mockZoom).toHaveBeenCalledWith(zoomOutRatio, ZOOM_TARGET_FRACTION);
  });
  it('calls zoom in with default ratio in when KeyW is pressed and no prop given', async () => {
    const { mockZoom } = await fireKeyEvent('w');
    expect(mockZoom).toHaveBeenCalledWith(ZOOM_IN_RATIO, ZOOM_TARGET_FRACTION);
  });
  it('calls zoom out with default ratio when KeyS is pressed and no prop given', async () => {
    const { mockZoom } = await fireKeyEvent('s');
    expect(mockZoom).toHaveBeenCalledWith(ZOOM_OUT_RATIO, ZOOM_TARGET_FRACTION);
  });
});
