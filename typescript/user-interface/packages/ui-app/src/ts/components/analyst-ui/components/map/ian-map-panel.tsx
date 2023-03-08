/* eslint-disable complexity */
import { Icon } from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import type { SignalDetectionTypes, StationTypes } from '@gms/common-model';
import {
  mapActions,
  useAppDispatch,
  useAppSelector,
  useGetSelectedSdIds,
  useStationsVisibility
} from '@gms/ui-state';
import React from 'react';

import { IanMapDataSource } from '~analyst-ui/components/map/ian-map-data-source';
import {
  layerDisplayStrings,
  layerSettings
} from '~analyst-ui/components/map/layer-selector-drawer/layer-selector-static-content';
import { messageConfig } from '~analyst-ui/config/message-config';
import { MapLayerPanelDrawer } from '~common-ui/components/map/map-layer-panel-drawer';
import { Map } from '~components/common-ui/components/map';
import { FEATURE_TOGGLES } from '~config/feature-toggles';
import { MAP_MIN_HEIGHT_PX } from '~data-acquisition-ui/components/soh-map/constants';

import { IANConfirmOpenEventPopup } from '../events/confirm-open-event-popup';
import { EdgeTypes } from '../events/types';
import {
  useMapEventConfidenceEllipseSources,
  useMapEventCoverageEllipseSources,
  useMapEventLocationSources,
  useMapSignalDetectionSources,
  useMapSiteSource,
  useMapStationSource
} from './create-ian-map-data-sources';
import { useHideShowContextMenuState, useStationOnClickHandler } from './ian-map-hooks';
import { IanMapTooltipHandler } from './ian-map-tooltip-handler';
import {
  clearEventTooltip,
  clearHoverTooltip,
  ianMapEventTooltipLabel,
  ianMapStationTooltipLabel,
  setViewer
} from './ian-map-tooltip-utils';
import { getStationOnRightClickHandler } from './ian-map-utils';
import type { MapEventSource } from './types';

export interface IANMapPanelProps {
  stationsResult: StationTypes.Station[];
  stationMount: () => void;
  signalDetections: SignalDetectionTypes.SignalDetection[];
  signalDetectionMount: () => void;
  preferredEventsResult: MapEventSource[];
  preferredEventMount: () => void;
  nonPreferredEventsResult: MapEventSource[];
  nonPreferredEventMount: () => void;
}
/**
 * IAN Map component. Renders a Cesium map and queries for Station Groups
 */
// eslint-disable-next-line react/function-component-definition
export const IANMapPanelComponent: React.FunctionComponent<IANMapPanelProps> = (
  props: IANMapPanelProps
) => {
  const {
    stationsResult,
    stationMount,
    signalDetections,
    signalDetectionMount,
    preferredEventsResult,
    preferredEventMount,
    nonPreferredEventsResult,
    nonPreferredEventMount
  } = props;

  const allowMultiSelect = FEATURE_TOGGLES.IAN_MAP_MULTI_SELECT;
  const selectedStations = useAppSelector(state => state.app.common?.selectedStationIds);
  const selectedEvents = useAppSelector(state => state.app.analyst?.selectedEventIds);
  const selectedSdIds = useGetSelectedSdIds();
  const isSyncedWithWaveformZoom = useAppSelector(state => state.app.map.isSyncedWithWaveformZoom);
  const dispatch = useAppDispatch();

  // Use custom hook for the on-left-click handler for IAN entities displayed on the map.
  const onStationClickHandler = useStationOnClickHandler();

  const [isDrawerOpen, setDrawerOpen] = React.useState(false);
  const [eventId, setEventId] = React.useState(undefined);
  const [isCurrentlyOpen, setIsCurrentlyOpen] = React.useState(false);

  const layerVisibility = useAppSelector(state => state.app.map.layerVisibility);

  const onCheckedCallback = React.useCallback(
    (checkedItem: string) => {
      if (checkedItem === messageConfig.labels.syncToWaveformDisplayVisibleTimeRange) {
        dispatch(mapActions.setIsMapSyncedWithWaveformZoom(!isSyncedWithWaveformZoom));
      } else {
        const newLayerVisibilty = { ...layerVisibility };
        newLayerVisibilty[layerDisplayStrings.keyOf(checkedItem)] = !layerVisibility[
          layerDisplayStrings.keyOf(checkedItem)
        ];
        dispatch(mapActions.updateLayerVisibility(newLayerVisibilty));
      }
    },
    [dispatch, isSyncedWithWaveformZoom, layerVisibility]
  );

  // on mount use effect
  React.useEffect(
    () => {
      document.addEventListener('keydown', clearHoverTooltip);

      return () => {
        document.removeEventListener('keydown', clearEventTooltip);
        document.removeEventListener('keydown', clearHoverTooltip);
        // clear out the viewer ref to prevent memory leak
        setViewer(null);
      };
    },
    // We only want this to run onMount so we need no dependencies
    // eslint-disable-next-line react-hooks/exhaustive-deps
    []
  );

  const { stationsVisibility, setStationVisibility, isStationVisible } = useStationsVisibility();

  const canShowContextMenu = useHideShowContextMenuState();

  const rightClickHandler = React.useMemo(
    () => getStationOnRightClickHandler(setStationVisibility, isStationVisible, canShowContextMenu),
    [canShowContextMenu, isStationVisible, setStationVisibility]
  );

  const canvas = document.getElementsByClassName('cesium-widget');
  // if the canvas exists set it to focusable
  if (canvas.length > 0) {
    canvas[0].setAttribute('tabindex', '0');
  }

  // Build data sources using custom hooks to split them into separate toggle data sources

  // Signal Detections
  const [
    intervalUnassociatedSignalDetectionDataSource,
    intervalAssociatedCompleteSignalDetectionDataSource,
    intervalAssociatedOpenSignalDetectionDataSource,
    intervalAssociatedOtherSignalDetectionDataSource
  ] = useMapSignalDetectionSources(
    signalDetections,
    signalDetectionMount,
    layerVisibility,
    stationsResult,
    EdgeTypes.INTERVAL
  );

  const [
    beforeUnassociatedSignalDetectionDataSource,
    beforeAssociatedCompleteSignalDetectionDataSource,
    beforeAssociatedOpenSignalDetectionDataSource,
    beforeAssociatedOtherSignalDetectionDataSource
  ] = useMapSignalDetectionSources(
    signalDetections,
    signalDetectionMount,
    layerVisibility,
    stationsResult,
    EdgeTypes.BEFORE
  );

  const [
    afterUnassociatedSignalDetectionDataSource,
    afterAssociatedCompleteSignalDetectionDataSource,
    afterAssociatedOpenSignalDetectionDataSource,
    afterAssociatedOtherSignalDetectionDataSource
  ] = useMapSignalDetectionSources(
    signalDetections,
    signalDetectionMount,
    layerVisibility,
    stationsResult,
    EdgeTypes.AFTER
  );

  // Event Locations
  const [
    nonPreferredAfterEventLocationDataSource,
    nonPreferredBeforeEventLocationDataSource,
    nonPreferredIntervalEventLocationDataSource,
    preferredAfterEventLocationDataSource,
    preferredBeforeEventLocationDataSource,
    preferredIntervalEventLocationDataSource
  ] = useMapEventLocationSources(
    layerVisibility,
    preferredEventMount,
    preferredEventsResult,
    nonPreferredEventMount,
    nonPreferredEventsResult,
    setEventId
  );

  // Coverage Ellipses
  const [
    nonPreferredAfterEventCoverageDataSource,
    nonPreferredBeforeEventCoverageDataSource,
    nonPreferredIntervalEventCoverageDataSource,
    preferredAfterEventCoverageDataSource,
    preferredBeforeEventCoverageDataSource,
    preferredIntervalEventCoverageDataSource
  ] = useMapEventCoverageEllipseSources(
    layerVisibility,
    preferredEventsResult,
    nonPreferredEventsResult,
    setEventId
  );

  // Confidence Ellipses
  const [
    nonPreferredAfterEventConfidenceDataSource,
    nonPreferredBeforeEventConfidenceDataSource,
    nonPreferredIntervalEventConfidenceDataSource,
    preferredAfterEventConfidenceDataSource,
    preferredBeforeEventConfidenceDataSource,
    preferredIntervalEventConfidenceDataSource
  ] = useMapEventConfidenceEllipseSources(
    layerVisibility,
    preferredEventsResult,
    nonPreferredEventsResult,
    setEventId
  );

  // Stations and sites

  // Put stations in their own data source to prevent z-index bugs occurring between stations and sites
  // create entities from stations array

  const stationDataSource = useMapStationSource(
    layerVisibility,
    stationsResult,
    stationsVisibility,
    stationMount,
    onStationClickHandler,
    rightClickHandler
  );

  // Put sites in their own data source to prevent z-index bugs occurring between stations and sites
  const siteDataSource = useMapSiteSource(
    layerVisibility,
    stationsResult,
    onStationClickHandler,
    rightClickHandler
  );

  const tooltipDataSource = (
    <IanMapDataSource
      key="Tooltip"
      entities={[ianMapStationTooltipLabel, ianMapEventTooltipLabel]}
      name="Tooltip"
      show
    />
  );

  const layerSelectionEntries = layerSettings(
    onCheckedCallback,
    layerVisibility,
    isSyncedWithWaveformZoom
  );

  return (
    <div className="ian-map-wrapper">
      <button
        type="button"
        id="layer-panel-button"
        className="map__layer-button cesium-button cesium-toolbar-button"
        title="Select Map Layers"
        onClick={() => setDrawerOpen(!isDrawerOpen)}
      >
        <Icon icon={IconNames.LAYERS} />
      </button>
      <IANConfirmOpenEventPopup
        isCurrentlyOpen={isCurrentlyOpen}
        setIsCurrentlyOpen={setIsCurrentlyOpen}
        eventId={eventId}
        setEventId={setEventId}
        parentComponentId="map"
      />
      <Map
        doMultiSelect={allowMultiSelect}
        selectedStations={selectedStations}
        selectedEvents={selectedEvents}
        selectedSdIds={selectedSdIds}
        dataSources={[
          stationDataSource,
          siteDataSource,
          tooltipDataSource,
          // signal detection sources
          intervalAssociatedCompleteSignalDetectionDataSource,
          intervalAssociatedOpenSignalDetectionDataSource,
          intervalAssociatedOtherSignalDetectionDataSource,
          intervalUnassociatedSignalDetectionDataSource,
          beforeUnassociatedSignalDetectionDataSource,
          beforeAssociatedCompleteSignalDetectionDataSource,
          beforeAssociatedOpenSignalDetectionDataSource,
          beforeAssociatedOtherSignalDetectionDataSource,
          afterUnassociatedSignalDetectionDataSource,
          afterAssociatedCompleteSignalDetectionDataSource,
          afterAssociatedOpenSignalDetectionDataSource,
          afterAssociatedOtherSignalDetectionDataSource,
          // preferred sources
          preferredIntervalEventLocationDataSource,
          preferredBeforeEventLocationDataSource,
          preferredAfterEventLocationDataSource,
          preferredIntervalEventCoverageDataSource,
          preferredBeforeEventCoverageDataSource,
          preferredAfterEventCoverageDataSource,
          preferredBeforeEventConfidenceDataSource,
          preferredIntervalEventConfidenceDataSource,
          preferredAfterEventConfidenceDataSource,
          // non-preferred sources
          nonPreferredIntervalEventCoverageDataSource,
          nonPreferredBeforeEventCoverageDataSource,
          nonPreferredAfterEventCoverageDataSource,
          nonPreferredIntervalEventLocationDataSource,
          nonPreferredBeforeEventLocationDataSource,
          nonPreferredAfterEventLocationDataSource,
          nonPreferredBeforeEventConfidenceDataSource,
          nonPreferredIntervalEventConfidenceDataSource,
          nonPreferredAfterEventConfidenceDataSource
        ]}
        minHeightPx={MAP_MIN_HEIGHT_PX}
        handlers={[IanMapTooltipHandler]}
      />
      <MapLayerPanelDrawer
        layerSelectionEntries={layerSelectionEntries}
        isDrawerOpen={isDrawerOpen}
        onDrawerClose={() => setDrawerOpen(false)}
        drawerClassName="ian-select-map-layers"
        title="Select Map Layers"
        checkboxOnChangeCallback={onCheckedCallback}
      />
    </div>
  );
};

/**
 * If map entities change, reload map display
 * Extracted for readability and testing
 *
 * @param prevProps
 * @param nextProps
 */
export const ianMapPanelMemoCheck = (
  prevProps: IANMapPanelProps,
  nextProps: IANMapPanelProps
): boolean => {
  // if false, reload
  if (!prevProps?.stationsResult || !prevProps?.signalDetections) {
    return false;
  }
  // if stations have changed reload
  if (nextProps?.stationsResult && prevProps.stationsResult !== nextProps.stationsResult) {
    return false;
  }

  // if signal detections have changed reload
  if (nextProps?.signalDetections && prevProps.signalDetections !== nextProps.signalDetections) {
    return false;
  }
  // if preferred events have changed reload
  if (
    nextProps?.preferredEventsResult &&
    prevProps.preferredEventsResult !== nextProps.preferredEventsResult
  ) {
    return false;
  }
  // if non-preferred events have changed reload
  if (
    nextProps?.nonPreferredEventsResult &&
    prevProps.nonPreferredEventsResult !== nextProps.nonPreferredEventsResult
  ) {
    return false;
  }
  return true;
};

export const IANMapPanel = React.memo(IANMapPanelComponent, ianMapPanelMemoCheck);
