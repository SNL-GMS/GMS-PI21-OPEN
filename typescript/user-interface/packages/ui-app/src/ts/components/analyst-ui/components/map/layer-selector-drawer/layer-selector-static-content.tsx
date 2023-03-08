import { Position } from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import type { CheckboxEntry } from '@gms/ui-core-components/lib/components/ui-widgets/checkbox-list/types';
import { MapLayers } from '@gms/ui-state/lib/app/state/map/types';
import Immutable from 'immutable';
import React from 'react';

import { MapLayerSettingsPopout } from '~analyst-ui/components/map/layer-selector-drawer/layer-settings-popout';
import { messageConfig } from '~analyst-ui/config/message-config';

import { DottedLineIcon } from '../img/dotted-line-icon';

export const layerDisplayStrings: Immutable.Map<MapLayers, string> = Immutable.Map<
  MapLayers,
  string
>([
  [MapLayers.stations, 'Stations'],
  [MapLayers.sites, 'Sites'],
  [MapLayers.signalDetections, 'Signal Detections'],
  [MapLayers.events, 'Events'],
  [MapLayers.preferredLocationSolution, 'Preferred location solution'],
  [MapLayers.edgeEventsBeforeInterval, 'Edge events before interval'],
  [MapLayers.edgeEventsAfterInterval, 'Edge events after interval'],
  [MapLayers.nonPreferredLocationSolution, 'Non-preferred location solutions'],
  [MapLayers.confidenceEllipse, 'Location uncertainty confidence ellipse'],
  [MapLayers.coverageEllipse, 'Location uncertainty coverage ellipse'],
  [MapLayers.edgeDetectionBefore, 'Edge detections before interval'],
  [MapLayers.edgeDetectionAfter, 'Edge detections after interval'],
  [MapLayers.unassociatedDetection, 'Unassociated'],
  [MapLayers.associatedOpenDetection, 'Associated to open event'],
  [MapLayers.associatedOtherDetection, 'Associated to other event'],
  [MapLayers.associatedCompleteDetection, 'Associated to completed event']
]);

export const signalDetectionSettings = (
  onCheckedCallback: (name: string) => void,
  layerVisibility: Record<MapLayers, boolean>,
  isSyncedWithWaveformZoom: boolean
) => {
  const signalDetectionsSettingsEntries: CheckboxEntry[] = [
    {
      name: messageConfig.labels.syncToWaveformDisplayVisibleTimeRange,
      isChecked: isSyncedWithWaveformZoom,
      divider: true
    },
    {
      name: layerDisplayStrings.get(MapLayers.edgeDetectionBefore),
      headerTitle: 'Edge Detections',
      isChecked: layerVisibility[MapLayers.edgeDetectionBefore]
    },
    {
      name: layerDisplayStrings.get(MapLayers.edgeDetectionAfter),
      isChecked: layerVisibility[MapLayers.edgeDetectionAfter],
      divider: true
    },
    {
      name: layerDisplayStrings.get(MapLayers.associatedOpenDetection),
      headerTitle: 'Association Status',
      isChecked: layerVisibility[MapLayers.associatedOpenDetection]
    },
    {
      name: layerDisplayStrings.get(MapLayers.associatedCompleteDetection),
      isChecked: layerVisibility[MapLayers.associatedCompleteDetection]
    },
    {
      name: layerDisplayStrings.get(MapLayers.associatedOtherDetection),
      isChecked: layerVisibility[MapLayers.associatedOtherDetection]
    },
    {
      name: layerDisplayStrings.get(MapLayers.unassociatedDetection),
      isChecked: layerVisibility[MapLayers.unassociatedDetection]
    }
  ];
  return (
    <MapLayerSettingsPopout
      settingsEntries={signalDetectionsSettingsEntries}
      onCheckedCallback={onCheckedCallback}
    />
  );
};

export const eventsSettings = (
  onCheckedCallback: (name: string) => void,
  layerVisibility: Record<MapLayers, boolean>
): React.ReactElement<any, any> => {
  const eventSettingsEntries: CheckboxEntry[] = [
    {
      name: layerDisplayStrings.get(MapLayers.edgeEventsBeforeInterval),
      headerTitle: 'Edge Events',
      isChecked: layerVisibility[MapLayers.edgeEventsBeforeInterval]
    },
    {
      name: layerDisplayStrings.get(MapLayers.edgeEventsAfterInterval),
      isChecked: layerVisibility[MapLayers.edgeEventsAfterInterval],
      divider: true
    },
    {
      name: layerDisplayStrings.get(MapLayers.preferredLocationSolution),
      headerTitle: 'Location Solutions',
      isChecked: layerVisibility[MapLayers.preferredLocationSolution],
      iconName: IconNames.FULL_CIRCLE,
      iconColor: 'white'
    },
    {
      name: layerDisplayStrings.get(MapLayers.nonPreferredLocationSolution),
      isChecked: layerVisibility[MapLayers.nonPreferredLocationSolution],
      divider: true,
      iconName: IconNames.CIRCLE,
      iconColor: 'black'
    },
    {
      name: layerDisplayStrings.get(MapLayers.confidenceEllipse),
      headerTitle: 'Location Uncertainty Ellipses',
      isChecked: layerVisibility[MapLayers.confidenceEllipse],
      element: <DottedLineIcon color="white" className="event-layer-dotted-line" />
    },
    {
      name: layerDisplayStrings.get(MapLayers.coverageEllipse),
      isChecked: layerVisibility[MapLayers.coverageEllipse],
      iconName: IconNames.MINUS,
      iconColor: 'white'
    }
  ];

  return (
    <MapLayerSettingsPopout
      settingsEntries={eventSettingsEntries}
      onCheckedCallback={onCheckedCallback}
    />
  );
};

export const layerSettings = (
  onCheckedCallback: (name: string) => void,
  layerVisibility: Record<MapLayers, boolean>,
  isSyncedWithWaveformZoom: boolean
): CheckboxEntry[] => {
  return [
    {
      name: layerDisplayStrings.get(MapLayers.stations),
      isChecked: layerVisibility[MapLayers.stations]
    },
    {
      name: layerDisplayStrings.get(MapLayers.sites),
      isChecked: layerVisibility[MapLayers.sites]
    },
    {
      name: layerDisplayStrings.get(MapLayers.signalDetections),
      isChecked: layerVisibility[MapLayers.signalDetections],
      iconButton: {
        iconName: IconNames.COG,
        popover: {
          // Callback/hook to switch intervals between zoomInterval and un-synced modes
          content: signalDetectionSettings(
            onCheckedCallback,
            layerVisibility,
            isSyncedWithWaveformZoom
          ),
          position: Position.RIGHT_BOTTOM,
          usePortal: true,
          minimal: true
        }
      }
    },
    {
      name: layerDisplayStrings.get(MapLayers.events),
      isChecked: layerVisibility[MapLayers.events],
      iconButton: {
        iconName: IconNames.COG,
        popover: {
          content: eventsSettings(onCheckedCallback, layerVisibility),
          position: Position.RIGHT_BOTTOM,
          usePortal: true,
          minimal: true
        }
      }
    }
  ];
};
