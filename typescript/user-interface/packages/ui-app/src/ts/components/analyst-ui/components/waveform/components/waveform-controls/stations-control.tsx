import { Button, Popover } from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import type { StationTypes } from '@gms/common-model';
import type { CheckboxSearchListTypes, ToolbarTypes } from '@gms/ui-core-components';
import { CheckboxSearchList, CustomToolbarItem } from '@gms/ui-core-components';
import { useStationsVisibility } from '@gms/ui-state';
import clone from 'lodash/clone';
import * as React from 'react';

import { useStationsVisibilityFromCheckboxState, useWaveformStations } from '../../waveform-hooks';

const SELECT_WIDTH_PX = 94;
const buttonStyle = { width: SELECT_WIDTH_PX };

export interface StationControlsProps {
  checkboxItems: CheckboxSearchListTypes.CheckboxItem[];
  setCheckboxItems: React.Dispatch<React.SetStateAction<CheckboxSearchListTypes.CheckboxItem[]>>;
}
/**
 * Given id and new checked value values returns a function that returns an
 * updated check box list
 *
 * @param id station id
 * @param newCheckedVal new checked value
 * @returns function
 */
export const buildUpdatedListFunc = (id: string, newCheckedVal: boolean) => {
  return (
    previousList: CheckboxSearchListTypes.CheckboxItem[]
  ): CheckboxSearchListTypes.CheckboxItem[] => {
    const checkboxItemsList: CheckboxSearchListTypes.CheckboxItem[] = [...previousList];
    const index = checkboxItemsList.findIndex(checkBoxItem => checkBoxItem.id === id);
    checkboxItemsList[index] = clone(checkboxItemsList[index]);
    checkboxItemsList[index].checked = newCheckedVal;
    return checkboxItemsList;
  };
};
/**
 * popover button with a checkbox search list
 */
// eslint-disable-next-line react/function-component-definition
export const StationControls: React.FC<StationControlsProps> = ({
  checkboxItems,
  setCheckboxItems
}: StationControlsProps) => {
  return (
    <Popover
      content={
        <CheckboxSearchList
          items={checkboxItems}
          // can't abstract away the whole function due to how the toolbar is built
          onCheckboxChecked={(id: string, newCheckedVal: boolean) => {
            setCheckboxItems(buildUpdatedListFunc(id, newCheckedVal));
          }}
          maxHeightPx={200}
        />
      }
    >
      <Button
        value="Stations"
        title="Select stations to be shown"
        alignText="left"
        rightIcon={IconNames.CARET_DOWN}
        style={buttonStyle}
        data-cy="stations-dropdown"
      >
        Stations
      </Button>
    </Popover>
  );
};

/**
 * Creates a toolbar item of a popover with a searchable checkbox item component
 *
 * @param checkboxItems the items to make checkboxes out of
 * @param setCheckboxItems callback function to change what is checked
 * @param key must be unique
 * @returns toolbar item
 */
export const buildStationsDropdown = (
  checkboxItems: CheckboxSearchListTypes.CheckboxItem[],
  setCheckboxItems: React.Dispatch<React.SetStateAction<CheckboxSearchListTypes.CheckboxItem[]>>,
  key: string | number
): ToolbarTypes.ToolbarItemElement => (
  <CustomToolbarItem
    key={key}
    label="Select stations"
    tooltip="Check a station to display"
    element={<StationControls checkboxItems={checkboxItems} setCheckboxItems={setCheckboxItems} />}
  />
);

/**
 * Toolbar control that lets the user choose what stations are shown in a stations dropdown with
 * checkboxes
 *
 * @param key must be unique
 * @returns the toolbar item
 */
export const useStationsDropdownControl = (
  key: string | number
): ToolbarTypes.ToolbarItemElement => {
  const [checkboxItemsList, setCheckboxItemsList] = React.useState<
    CheckboxSearchListTypes.CheckboxItem[]
  >([]);
  const { isStationVisible } = useStationsVisibility();

  const setStationsVisibilityFromCheckboxState = useStationsVisibilityFromCheckboxState(
    checkboxItemsList
  );
  const stationDefResult = useWaveformStations();
  React.useEffect(() => {
    const checkboxItemsListNew: CheckboxSearchListTypes.CheckboxItem[] = [];
    stationDefResult.data?.forEach((s: StationTypes.Station) => {
      checkboxItemsListNew.push({
        id: s.name,
        name: s.name,
        checked: isStationVisible(s.name)
      });
    });
    checkboxItemsListNew.sort((a, b) => `${a.name}`.localeCompare(b.name));
    setCheckboxItemsList(checkboxItemsListNew);
  }, [stationDefResult.data, isStationVisible]);

  return React.useMemo(
    () => buildStationsDropdown(checkboxItemsList, setStationsVisibilityFromCheckboxState, key),
    [checkboxItemsList, key, setStationsVisibilityFromCheckboxState]
  );
};
