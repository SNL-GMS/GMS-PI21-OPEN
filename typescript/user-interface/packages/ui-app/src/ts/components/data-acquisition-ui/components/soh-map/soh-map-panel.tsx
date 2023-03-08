/* eslint-disable react/destructuring-assignment */
import { H5, Icon, Position, Radio } from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import type { CheckboxListEntry } from '@gms/ui-core-components';
import { nonIdealStateWithSpinner, SimpleCheckboxList } from '@gms/ui-core-components';
import type { CheckboxEntry } from '@gms/ui-core-components/lib/components/ui-widgets/checkbox-list/types';
import { useProcessingStations } from '@gms/ui-state';
import { useImmutableMap } from '@gms/ui-util';
import * as Cesium from 'cesium';
import * as Immutable from 'immutable';
import * as React from 'react';
import { Entity } from 'resium';

import { MapLayerPanelDrawer } from '~common-ui/components/map/map-layer-panel-drawer';
import { Map } from '~components/common-ui/components/map';

import { mapStationsToSohEntities } from './station-data-source';
import type { SohMapPanelProps } from './types';

/** defines the station layer label */
const stationLayer = 'Stations';

/** defines the available layer options displayed in the drawer */
const layerOptions = [stationLayer];
/**
 * SOH map panel component
 *
 * @param props the props
 */
export function SohMapPanel(props: SohMapPanelProps) {
  const stationSoh = props.sohStatus?.stationAndStationGroupSoh;
  const stationGroupNames = React.useMemo(
    () => stationSoh.stationGroups?.map(entry => entry.stationGroupName),
    [stationSoh.stationGroups]
  );
  const [isDrawerOpen, setDrawerOpen] = React.useState(false);
  const [layerVisibilityMap, setLayerVisibilityMap] = useImmutableMap<boolean>(layerOptions, true);
  const [stationGroupVisibilityMap, setStationVisibilityMap] = useImmutableMap<boolean>(
    stationGroupNames,
    true
  );

  // If station group names changed and we haven't already built the map build map
  React.useEffect(() => {
    if (stationGroupVisibilityMap.size !== stationGroupNames.length) {
      let map = Immutable.Map<string, boolean>();
      stationGroupNames.forEach(name => {
        map = map.set(name, true);
      });
      setStationVisibilityMap(map);
    }
  }, [stationGroupNames, stationGroupVisibilityMap, setStationVisibilityMap]);

  const [showSohStatus, setShowSohStatus] = React.useState(true);
  const processingStations = useProcessingStations();

  if (processingStations.length === 0 || stationGroupVisibilityMap.size === 0) {
    return nonIdealStateWithSpinner('Loading', 'Stations');
  }

  // checklist of station group names
  const stationGroupSelectionEntries = stationGroupNames?.map(name => {
    const checkboxListEntry: CheckboxListEntry = {
      name,
      isChecked: stationGroupVisibilityMap.get(name)
    };
    return checkboxListEntry;
  });

  // popover content for the station icon button
  const content = (
    <div className="icon-cell-content__container">
      <div id="left" className="icon-cell-content__left">
        <div>
          <H5 className="icon-cell-content__left--header">Filter by Station Group</H5>
        </div>
        <div className="icon-cell-content__left--checkbox-list">
          <SimpleCheckboxList
            checkBoxListEntries={stationGroupSelectionEntries}
            onChange={item =>
              setStationVisibilityMap(
                stationGroupVisibilityMap.set(item, !stationGroupVisibilityMap.get(item))
              )
            }
          />
        </div>
      </div>
      <div id="right" className="icon-cell-content__right">
        <H5 className="icon-cell-content__right--header">Station Color</H5>
        <div className="icon-cell-content__right--radio-list">
          <Radio
            label="Soh Status"
            value="sohStatus"
            key="sohStatus"
            className="alignment-dropdown__radio"
            checked={showSohStatus}
            onChange={() => setShowSohStatus(!showSohStatus)}
          />
          <Radio
            label="Capability Status"
            value="capabilityStatus"
            key="capabilityStatus"
            className="alignment-dropdown__radio"
            checked={!showSohStatus}
            onChange={() => setShowSohStatus(!showSohStatus)}
          />
        </div>
      </div>
    </div>
  );

  // layer selection entries
  const layerSelectionEntries: CheckboxEntry[] = [
    {
      name: stationLayer,
      isChecked: layerVisibilityMap.get(stationLayer),
      iconButton: {
        iconName: IconNames.COG,
        popover: {
          content,
          position: Position.RIGHT_BOTTOM,
          usePortal: false,
          minimal: true
        }
      }
    }
  ];

  // filter out stations based on the selected groups
  const filteredStationSoh = props.sohStatus.stationAndStationGroupSoh.stationSoh.filter(s =>
    s.stationGroups
      .map(g => g.groupName)
      .find(n => stationGroupVisibilityMap.has(n) && stationGroupVisibilityMap.get(n))
  );

  // map stations to cesium entities
  const entities = mapStationsToSohEntities(
    processingStations,
    filteredStationSoh,
    props.selectedStationIds,
    showSohStatus
  );
  const onClickHandler = (targetEntity: Cesium.Entity) => () => {
    props.setSelectedStationIds([targetEntity.id]);
  };
  // map cesium entities to resium components
  const entityComponents = entities.map((sohEntity: Cesium.Entity) => {
    return (
      <Entity
        id={sohEntity.id}
        label={sohEntity.label}
        key={sohEntity.id}
        name={sohEntity.name}
        billboard={sohEntity.billboard}
        show={sohEntity.show}
        properties={sohEntity.properties}
        position={sohEntity.position.getValue(Cesium.JulianDate.now())}
        onClick={onClickHandler(sohEntity)}
      />
    );
  });
  return (
    <div className="soh-map-wrapper">
      <button
        type="button"
        id="layer-panel-button"
        className="map__layer-button cesium-button cesium-toolbar-button"
        title="Select Map Layers"
        onClick={() => setDrawerOpen(!isDrawerOpen)}
      >
        <Icon icon={IconNames.LAYERS} />
      </button>
      <Map
        minHeightPx={props.minHeightPx}
        entities={layerVisibilityMap.get(stationLayer) ? entityComponents : []}
      />
      <MapLayerPanelDrawer
        layerSelectionEntries={layerSelectionEntries}
        isDrawerOpen={isDrawerOpen}
        onDrawerClose={() => setDrawerOpen(false)}
        drawerClassName="soh-legend"
        title="Select Map Layers"
        checkboxOnChangeCallback={layer =>
          setLayerVisibilityMap(layerVisibilityMap.set(layer, !layerVisibilityMap.get(layer)))
        }
      />
    </div>
  );
}
