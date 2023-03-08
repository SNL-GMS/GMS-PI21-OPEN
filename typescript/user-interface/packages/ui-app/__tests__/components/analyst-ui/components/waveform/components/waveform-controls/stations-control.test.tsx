/* eslint-disable react/jsx-props-no-spreading */

import type { CheckboxSearchListTypes, ToolbarTypes } from '@gms/ui-core-components';
import { isCustomToolbarItem } from '@gms/ui-core-components/lib/components/ui-widgets/toolbar/toolbar-item/custom-item';
import { getStore, waveformActions } from '@gms/ui-state';
import { render } from '@testing-library/react';
import * as React from 'react';
import { Provider } from 'react-redux';

import {
  buildStationsDropdown,
  buildUpdatedListFunc,
  StationControls,
  useStationsDropdownControl
} from '../../../../../../../src/ts/components/analyst-ui/components/waveform/components/waveform-controls/stations-control';

jest.mock(
  '../../../../../../../src/ts/components/analyst-ui/components/waveform/waveform-hooks',
  () => {
    const mockData = [{ name: 'name' }, { name: 'name2' }];
    const actualWaveformHooks = jest.requireActual(
      '../../../../../../../src/ts/components/analyst-ui/components/waveform/waveform-hooks'
    );
    return {
      ...actualWaveformHooks,
      useWaveformStations: () => ({
        data: mockData
      })
    };
  }
);

const store = getStore();

describe('station control', () => {
  it('is defined', () => {
    expect(buildUpdatedListFunc).toBeDefined();
    expect(StationControls).toBeDefined();
    expect(buildStationsDropdown).toBeDefined();
    expect(useStationsDropdownControl).toBeDefined();
  });
  const stationControlList: CheckboxSearchListTypes.CheckboxItem[] = [
    {
      id: 'id1',
      name: 'id1',
      checked: true
    },
    {
      id: 'id2',
      name: 'id2',
      checked: false
    }
  ];
  const callBackMock = jest.fn();

  it('StationControls matches a snapshot when given basic props', () => {
    const { container } = render(
      <StationControls checkboxItems={stationControlList} setCheckboxItems={callBackMock} />
    );
    expect(container).toMatchSnapshot();
  });
  it('buildUpdatedList matches a snapshot', () => {
    const updatedListFunc = buildUpdatedListFunc('id1', false);
    const updatedList = updatedListFunc(stationControlList);
    expect(updatedList[0].checked).toEqual(false);
    expect(updatedList).toMatchSnapshot();
  });
  it('buildStationsDropdown matches a snapshot', () => {
    expect(buildStationsDropdown(stationControlList, jest.fn(), 1)).toMatchSnapshot();
  });
  it('useStationsDropdownControl matches a snapshot', () => {
    function TestComponent() {
      const stationsDropdownControl: ToolbarTypes.ToolbarItemElement = useStationsDropdownControl(
        'teststations'
      );
      const { props: itemBase } = stationsDropdownControl;
      return isCustomToolbarItem(itemBase) ? itemBase.element : <div />;
    }
    store.dispatch(
      waveformActions.setStationsVisibility({
        name: {
          visibility: true,
          stationName: 'name',
          isStationExpanded: false,
          hiddenChannels: undefined
        },
        name2: {
          visibility: true,
          stationName: 'name2',
          isStationExpanded: false,
          hiddenChannels: undefined
        }
      })
    );
    const { container } = render(
      <Provider store={store}>
        <TestComponent />
      </Provider>
    );
    // This ensures that the axios request will have been called.
    expect(container).toMatchSnapshot();
  });
});
