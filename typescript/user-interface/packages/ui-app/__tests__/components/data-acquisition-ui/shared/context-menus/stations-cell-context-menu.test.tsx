import { ContextMenu } from '@blueprintjs/core';
import { Logger } from '@gms/common-util';
import React from 'react';
import { act } from 'react-dom/test-utils';

import type {
  StationSohContextMenuItemProps,
  StationSohContextMenuProps
} from '../../../../../src/ts/components/data-acquisition-ui/shared/context-menus/stations-cell-context-menu';
import {
  acknowledgeOnClick,
  DisabledStationSohContextMenu,
  getStationAcknowledgementMenuText,
  StationSohContextMenu
} from '../../../../../src/ts/components/data-acquisition-ui/shared/context-menus/stations-cell-context-menu';

const logger = Logger.create('GMS_LOG_JEST', process.env.GMS_LOG_JEST);

// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports
const Enzyme = require('enzyme');

describe('Station cell context menu', () => {
  const stationSohContextMenuProps: StationSohContextMenuProps = {
    stationNames: ['test', 'test2'],
    acknowledgeCallback: jest.fn()
  };

  const stationSohContextMenu = Enzyme.mount(
    // eslint-disable-next-line react/jsx-props-no-spreading
    <StationSohContextMenu {...stationSohContextMenuProps} />
  );
  const disabledStationSohContextMenu = Enzyme.mount(
    // eslint-disable-next-line react/jsx-props-no-spreading
    <DisabledStationSohContextMenu {...stationSohContextMenuProps} />
  );

  it('StationSohContextMenu can be created', () => {
    expect(stationSohContextMenu).toMatchSnapshot();
  });

  it('DisabledStationSohContextMenu can be created', () => {
    expect(disabledStationSohContextMenu).toMatchSnapshot();
  });
  it('getStationAcknowledgementMenuText to work as expected', () => {
    const stationNames = ['test', 'test1'];
    const withComment = false;
    const expectedResult = 'Acknowledge 2 stations';
    const result = getStationAcknowledgementMenuText(stationNames, withComment);
    expect(result).toEqual(expectedResult);
  });

  it('acknowledgeOnClick to work as expected', async () => {
    const stationSohContextMenuItemProps: StationSohContextMenuItemProps = {
      ...stationSohContextMenuProps,
      disabled: false
    };
    const createSpy = jest
      .spyOn(ContextMenu, 'show')
      .mockImplementation(() => logger.debug('shown'));
    const mouseEvent: any = {
      preventDefault: jest.fn(),
      clientX: 5,
      clientY: 5
    };
    // call acknowledge and wait for React lifecycle to finish so it can be deferred.
    await act(async () => {
      acknowledgeOnClick(mouseEvent, stationSohContextMenuItemProps);
      const waitDurationMs = 200;
      // eslint-disable-next-line no-promise-executor-return
      await new Promise(resolve => setTimeout(resolve, waitDurationMs));
    });
    expect(createSpy).toHaveBeenCalled();
  });
});
