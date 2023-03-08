import { Drawer, Position } from '@blueprintjs/core';
import type { CheckboxListEntry } from '@gms/ui-core-components';
import { SimpleCheckboxList } from '@gms/ui-core-components';
import * as React from 'react';

/**
 * Props for the panel drawer on the map
 *
 * @param isDrawerOpen simple boolean that determines if the panel is shown or not
 * @param layerSelectionEntries CheckboxListEntries to be shown in rows in the panel each with a button in their row
 * @param onDrawerClose callback to handle what happens when the drawer is closed
 * @param drawerClassName class to give the panel drawer
 * @param title Title is displayed on the top of the drawer
 * @param checkboxOnChangeCallback callback for when you check a box, has the checkbox item name as a parameter
 */
interface MapLayerPanelDrawerProps {
  isDrawerOpen: boolean;
  layerSelectionEntries: CheckboxListEntry[];
  onDrawerClose: () => void;
  drawerClassName: string;
  title: string;
  checkboxOnChangeCallback;
}

/**
 * This drawer is a layer selector for the map. When open it will display rows of checkboxes that allow you to show
 * or hide map layers
 *
 * @param props
 * @constructor
 */
// eslint-disable-next-line react/function-component-definition
export const MapLayerPanelDrawer: React.FunctionComponent<MapLayerPanelDrawerProps> = (
  props: MapLayerPanelDrawerProps
) => {
  const {
    isDrawerOpen,
    layerSelectionEntries,
    onDrawerClose,
    drawerClassName,
    title,
    checkboxOnChangeCallback
  } = props;

  return (
    <Drawer
      className={drawerClassName}
      title={title}
      isOpen={isDrawerOpen}
      autoFocus
      canEscapeKeyClose
      canOutsideClickClose
      enforceFocus={false}
      hasBackdrop={false}
      position={Position.LEFT}
      size={240}
      onClose={onDrawerClose}
      usePortal={false}
    >
      <SimpleCheckboxList
        checkBoxListEntries={layerSelectionEntries}
        onChange={checkboxOnChangeCallback}
      />
    </Drawer>
  );
};
