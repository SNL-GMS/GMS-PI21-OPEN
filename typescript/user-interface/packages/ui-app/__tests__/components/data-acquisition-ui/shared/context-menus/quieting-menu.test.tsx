import { ContextMenu } from '@blueprintjs/core';
import { SohTypes } from '@gms/common-model';
import { getStore } from '@gms/ui-state';
import { render } from '@testing-library/react';
import React from 'react';
import { Provider } from 'react-redux';

import type { QuietAction } from '../../../../../src/ts/components/data-acquisition-ui/shared/context-menus/quieting-menu';
import {
  CancelMenuItem,
  QuietWithCommentDialog,
  QuiteMenuItem,
  QuiteWithCommentMenuItem,
  showQuietingContextMenu
} from '../../../../../src/ts/components/data-acquisition-ui/shared/context-menus/quieting-menu';
import type { Offset } from '../../../../../src/ts/components/data-acquisition-ui/shared/types';
// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

describe('quieting menu', () => {
  const stationName = '';
  const channelPair: SohTypes.ChannelMonitorPair = {
    channelName: 'name',
    monitorType: SohTypes.SohMonitorType.MISSING
  };

  const position: Offset = {
    // eslint-disable-next-line @typescript-eslint/no-magic-numbers
    left: 10,
    // eslint-disable-next-line @typescript-eslint/no-magic-numbers
    top: 10
  };
  // eslint-disable-next-line @typescript-eslint/no-magic-numbers
  const quietingDurationSelections = [300000, 900000, 3600000, 86400000, 604800000];
  const quietUntilMs = undefined;
  const quietAction: QuietAction = {
    channelMonitorPairs: [channelPair],
    position,
    quietUntilMs,
    quietingDurationSelections,
    stationName
  };

  it('should have the show quieting menu function', () => {
    expect(showQuietingContextMenu).toBeDefined();
  });

  it('QuiteWithCommentDialog can be created', () => {
    const { container: quietWithCommentDialog } = render(
      <Provider store={getStore()}>
        {/* eslint-disable-next-line react/jsx-props-no-spreading */}
        <QuietWithCommentDialog {...quietAction} />
      </Provider>
    );
    expect(quietWithCommentDialog).toMatchSnapshot();
  });

  it('QuiteMenuItem can be created', () => {
    const { container: quietMenuItem } = render(
      <Provider store={getStore()}>
        {/* eslint-disable-next-line react/jsx-props-no-spreading */}
        <QuiteMenuItem {...quietAction} />
      </Provider>
    );
    expect(quietMenuItem).toMatchSnapshot();
  });

  it('QuiteWithCommentMenuItem can be created', () => {
    const { container: quiteWithCommentMenuItem } = render(
      <Provider store={getStore()}>
        {/* eslint-disable-next-line react/jsx-props-no-spreading */}
        <QuiteWithCommentMenuItem {...quietAction} />
      </Provider>
    );

    expect(quiteWithCommentMenuItem).toMatchSnapshot();
  });

  it('CancelMenuItem can be created', () => {
    const { container: cancelMenuItem } = render(
      <Provider store={getStore()}>
        {/* eslint-disable-next-line react/jsx-props-no-spreading */}
        <CancelMenuItem {...quietAction} />
      </Provider>
    );
    expect(cancelMenuItem).toMatchSnapshot();
  });

  it('should call the contextmenu.show function with menu', () => {
    ContextMenu.show = jest.fn() as any;
    showQuietingContextMenu(quietAction);
    expect(ContextMenu.show).toHaveBeenCalledTimes(1);
    expect((ContextMenu.show as any).mock.calls[0]).toMatchSnapshot();
  });
});
