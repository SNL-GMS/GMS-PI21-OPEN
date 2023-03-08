/* eslint-disable react/jsx-no-constructed-context-values */
import { getStore } from '@gms/ui-state';
import { render } from '@testing-library/react';
import * as React from 'react';
import { Provider } from 'react-redux';

import { MapLayerSettingsPopout } from '../../../../../src/ts/components/analyst-ui/components/map/layer-selector-drawer/layer-settings-popout';
import { BaseDisplayContext } from '../../../../../src/ts/components/common-ui/components/base-display';

const { container: settingsWrapper } = render(
  <Provider store={getStore()}>
    <BaseDisplayContext.Provider value={{ glContainer: {} as any, widthPx: 200, heightPx: 200 }}>
      <MapLayerSettingsPopout
        settingsEntries={[]}
        onCheckedCallback={() => console.log('check check')}
      />
    </BaseDisplayContext.Provider>
  </Provider>
);

describe('map settings panel', () => {
  test('is defined', () => {
    expect(MapLayerSettingsPopout).toBeDefined();
  });
  test('can mount settings popout', () => {
    expect(settingsWrapper).toMatchSnapshot();
  });
});
