/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable @typescript-eslint/no-magic-numbers */

import cloneDeep from 'lodash/cloneDeep';

import * as DragUtils from '../../src/ts/ui-util/drag-util';

const nativeEvent = new Event('drag');

const event = {
  nativeEvent
};

describe('Drag utils', () => {
  it('to be defined', () => {
    expect(DragUtils.DragEventType).toBeDefined();
    expect(DragUtils.DragEventType).toBeDefined();
    expect(DragUtils.dragEventIsOfType).toBeDefined();
    expect(DragUtils.getDragData).toBeDefined();
    expect(DragUtils.getNativeEvent).toBeDefined();
    expect(DragUtils.overrideDragCursor).toBeDefined();
    expect(DragUtils.storeDragData).toBeDefined();
  });

  it('can get event', () => {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    expect(DragUtils.getNativeEvent(nativeEvent as any)).toEqual(nativeEvent);
    expect(DragUtils.getNativeEvent(event as any)).toEqual(nativeEvent);
  });

  it('drag is not of type', () => {
    expect(
      DragUtils.dragEventIsOfType(undefined, DragUtils.DragEventType.APPLICATION_JSON)
    ).toBeFalsy();

    expect(
      DragUtils.dragEventIsOfType(
        { nativeEvent: undefined } as any,
        DragUtils.DragEventType.APPLICATION_JSON
      )
    ).toBeFalsy();

    expect(
      DragUtils.dragEventIsOfType(event as any, DragUtils.DragEventType.APPLICATION_JSON)
    ).toBeFalsy();

    expect(
      DragUtils.dragEventIsOfType(event as any, DragUtils.DragEventType.SOH_ACKNOWLEDGEMENT)
    ).toBeFalsy();

    const nonEventWithData = cloneDeep(event);
    // eslint-disable-next-line @typescript-eslint/dot-notation, dot-notation
    nonEventWithData.nativeEvent['dataTransfer'] = {
      types: JSON.stringify({
        'application/json': JSON.stringify({ data: 'test data' })
      })
    };

    expect(
      DragUtils.dragEventIsOfType(nonEventWithData as any, DragUtils.DragEventType.APPLICATION_JSON)
    ).toBeFalsy();

    expect(
      DragUtils.dragEventIsOfType(
        nonEventWithData as any,
        DragUtils.DragEventType.SOH_ACKNOWLEDGEMENT
      )
    ).toBeFalsy();
  });

  it('drag is of type', () => {
    const appJsonEvent = cloneDeep(event);
    // eslint-disable-next-line @typescript-eslint/dot-notation, dot-notation
    appJsonEvent.nativeEvent['dataTransfer'] = {
      types: JSON.stringify({
        'application/json': JSON.stringify({ data: 'test data' }),
        APPLICATION_JSON: true
      })
    };

    expect(
      DragUtils.dragEventIsOfType(appJsonEvent as any, DragUtils.DragEventType.APPLICATION_JSON)
    ).toBeTruthy();

    const sohEvent = cloneDeep(event);
    // eslint-disable-next-line @typescript-eslint/dot-notation, dot-notation
    appJsonEvent.nativeEvent['dataTransfer'] = {
      types: JSON.stringify({
        'application/json': JSON.stringify({ data: 'test data' }),
        soh_acknowledgement: true
      })
    };

    expect(
      DragUtils.dragEventIsOfType(sohEvent as any, DragUtils.DragEventType.SOH_ACKNOWLEDGEMENT)
    ).toBeTruthy();
  });

  it('store drag data', () => {
    const storeEvent = cloneDeep(event);
    // eslint-disable-next-line @typescript-eslint/dot-notation, dot-notation
    storeEvent.nativeEvent['dataTransfer'] = {};

    // eslint-disable-next-line @typescript-eslint/dot-notation, dot-notation
    storeEvent.nativeEvent['dataTransfer']['types'] = JSON.stringify({});

    // eslint-disable-next-line @typescript-eslint/dot-notation, dot-notation
    storeEvent.nativeEvent['dataTransfer']['setData'] = jest.fn((key: string, data: string) => {
      // eslint-disable-next-line @typescript-eslint/dot-notation, dot-notation
      const value = JSON.parse(storeEvent.nativeEvent['dataTransfer']['types']);
      value[key] = data;

      // eslint-disable-next-line @typescript-eslint/dot-notation, dot-notation
      storeEvent.nativeEvent['dataTransfer']['types'] = JSON.stringify(value);
    });

    DragUtils.storeDragData(storeEvent as any, 'my data', DragUtils.DragEventType.APPLICATION_JSON);

    expect(
      DragUtils.dragEventIsOfType(storeEvent as any, DragUtils.DragEventType.APPLICATION_JSON)
    ).toBeTruthy();

    expect(
      DragUtils.dragEventIsOfType(storeEvent as any, DragUtils.DragEventType.SOH_ACKNOWLEDGEMENT)
    ).toBeFalsy();

    // eslint-disable-next-line @typescript-eslint/dot-notation, dot-notation
    storeEvent.nativeEvent['dataTransfer']['types'] = JSON.stringify({});

    DragUtils.storeDragData(
      storeEvent as any,
      'my data',
      DragUtils.DragEventType.SOH_ACKNOWLEDGEMENT
    );

    expect(
      DragUtils.dragEventIsOfType(storeEvent as any, DragUtils.DragEventType.APPLICATION_JSON)
    ).toBeFalsy();

    expect(
      DragUtils.dragEventIsOfType(storeEvent as any, DragUtils.DragEventType.SOH_ACKNOWLEDGEMENT)
    ).toBeTruthy();

    // eslint-disable-next-line @typescript-eslint/dot-notation, dot-notation
    expect(storeEvent.nativeEvent['dataTransfer']['setData']).toBeCalledTimes(4);
  });

  it('get no drag data', () => {
    const noData = cloneDeep(event);

    expect(
      DragUtils.getDragData(noData as any, DragUtils.DragEventType.APPLICATION_JSON)
    ).toBeUndefined();
    expect(
      DragUtils.getDragData(noData as any, DragUtils.DragEventType.APPLICATION_JSON)
    ).toBeUndefined();
  });

  it('get drag data', () => {
    const data = { value: 'my data' };

    const dataEvent = cloneDeep(event);
    // eslint-disable-next-line @typescript-eslint/dot-notation, dot-notation
    dataEvent.nativeEvent['dataTransfer'] = {};

    // eslint-disable-next-line @typescript-eslint/dot-notation, dot-notation
    dataEvent.nativeEvent['dataTransfer']['types'] = JSON.stringify({});

    // eslint-disable-next-line @typescript-eslint/dot-notation, dot-notation
    dataEvent.nativeEvent['dataTransfer']['setData'] = jest.fn((k: string, d: string) => {
      // eslint-disable-next-line @typescript-eslint/dot-notation, dot-notation
      const value = JSON.parse(dataEvent.nativeEvent['dataTransfer']['types']);
      value[k] = d;

      // eslint-disable-next-line @typescript-eslint/dot-notation, dot-notation
      dataEvent.nativeEvent['dataTransfer']['types'] = JSON.stringify(value);
    });

    // eslint-disable-next-line @typescript-eslint/dot-notation, dot-notation
    dataEvent.nativeEvent['dataTransfer']['getData'] = jest.fn(() => JSON.stringify(data));

    DragUtils.storeDragData(dataEvent as any, data, DragUtils.DragEventType.APPLICATION_JSON);

    expect(
      DragUtils.getDragData(dataEvent as any, DragUtils.DragEventType.APPLICATION_JSON)
    ).not.toBeUndefined();
    expect(
      DragUtils.getDragData(dataEvent as any, DragUtils.DragEventType.APPLICATION_JSON)
    ).toEqual(data);
    expect(
      DragUtils.getDragData(dataEvent as any, DragUtils.DragEventType.SOH_ACKNOWLEDGEMENT)
    ).toBeUndefined();
  });

  it('store drag', () => {
    const dragImage = cloneDeep(event);
    // eslint-disable-next-line @typescript-eslint/dot-notation, dot-notation
    dragImage.nativeEvent['dataTransfer'] = {};

    // eslint-disable-next-line @typescript-eslint/dot-notation, dot-notation
    dragImage.nativeEvent['dataTransfer']['setDragImage'] = jest.fn();

    DragUtils.overrideDragCursor(dragImage as any, document.createElement('div'));

    // eslint-disable-next-line @typescript-eslint/dot-notation, dot-notation
    expect(dragImage.nativeEvent['dataTransfer']['setDragImage']).toBeCalledTimes(1);
  });

  it('off screen wrapper', () => {
    const wrapper = new DragUtils.OffScreenWrapper();

    expect(wrapper.getElement()).not.toBeUndefined();
    wrapper.append(document.createElement('div'));
    expect(wrapper.getElement()).not.toBeUndefined();

    (wrapper as any).renderOffScreen(document.createElement('div'));

    expect(document.getElementById((wrapper as any).id)).not.toBeUndefined();
    wrapper.destroy();
    expect(document.getElementById((wrapper as any).id)).toBeNull();
  });
});
