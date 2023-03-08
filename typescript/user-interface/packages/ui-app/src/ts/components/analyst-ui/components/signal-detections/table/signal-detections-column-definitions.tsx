import { EventTypes } from '@gms/common-model';
import type { AgGridCommunity, ColumnDefinition } from '@gms/ui-core-components';
import { SignalDetectionColumn } from '@gms/ui-state';

import { getAssocStatusString } from '~analyst-ui/common/utils/signal-detection-util';
import { signalDetectionsColumnsToDisplay } from '~analyst-ui/components/signal-detections/table/signal-detections-table-utils';
import type { SignalDetectionRow } from '~analyst-ui/components/signal-detections/types';
import { signalDetectionColumnDisplayStrings } from '~analyst-ui/components/signal-detections/types';
import { messageConfig } from '~analyst-ui/config/message-config';
import { largeCellWidthPx, medCellWidthPx } from '~common-ui/common/table-types';
import { caseInsensitiveComparator, numericStringComparator } from '~common-ui/common/table-utils';
import { unsavedChangesIcon } from '~resources/unsaved-changes-icon';

import { DirtyDotCellRenderer } from './signal-detections-dirty-dot';

const standardDeviationHeaderWidthPx = 130;

/* applies classes to table cells conditionally based on content, used in this case for styling event association cells */
const eventAssociationCellClassRules: AgGridCommunity.CellClassRules = {
  'sd-cell--open-associated': params =>
    params.value === EventTypes.AssociationStatus.OPEN_ASSOCIATED,
  'sd-cell--complete-associated': params =>
    params.value === EventTypes.AssociationStatus.COMPLETE_ASSOCIATED,
  'sd-cell--other-associated': params =>
    params.value === EventTypes.AssociationStatus.OTHER_ASSOCIATED,
  'sd-cell--unassociated': params => params.value === EventTypes.AssociationStatus.UNASSOCIATED
};

function SDAssociationStatusTooltipValueGetter(params) {
  const tooltip = getAssocStatusString(params.value);
  if (tooltip === messageConfig.invalidCellText) return params.value;
  return tooltip;
}

export const signalDetectionsColumnDefs: ColumnDefinition<
  SignalDetectionRow,
  unknown,
  unknown,
  unknown,
  unknown
>[] = [
  {
    headerComponentParams: {
      template: unsavedChangesIcon
    },
    field: SignalDetectionColumn.unsavedChanges,
    headerTooltip: signalDetectionColumnDisplayStrings.get(SignalDetectionColumn.unsavedChanges),
    hide: !signalDetectionsColumnsToDisplay.get(SignalDetectionColumn.unsavedChanges),
    cellRendererFramework: DirtyDotCellRenderer,
    width: 30
  },
  {
    headerName: signalDetectionColumnDisplayStrings.get(SignalDetectionColumn.assocStatus),
    field: SignalDetectionColumn.assocStatus,
    headerTooltip: 'Event association status',
    cellClassRules: eventAssociationCellClassRules,
    hide: !signalDetectionsColumnsToDisplay.get(SignalDetectionColumn.assocStatus),
    tooltipValueGetter: params => SDAssociationStatusTooltipValueGetter(params),
    width: 83
  },
  {
    headerName: signalDetectionColumnDisplayStrings.get(SignalDetectionColumn.conflict),
    field: SignalDetectionColumn.conflict,
    headerTooltip: signalDetectionColumnDisplayStrings.get(SignalDetectionColumn.conflict),
    hide: !signalDetectionsColumnsToDisplay.get(SignalDetectionColumn.conflict)
  },
  {
    headerName: signalDetectionColumnDisplayStrings.get(SignalDetectionColumn.station),
    field: SignalDetectionColumn.station,
    cellClass: 'monospace',
    headerTooltip: signalDetectionColumnDisplayStrings.get(SignalDetectionColumn.station),
    hide: !signalDetectionsColumnsToDisplay.get(SignalDetectionColumn.station)
  },
  {
    headerName: signalDetectionColumnDisplayStrings.get(SignalDetectionColumn.channel),
    field: SignalDetectionColumn.channel,
    cellClass: 'monospace',
    headerTooltip: signalDetectionColumnDisplayStrings.get(SignalDetectionColumn.channel),
    hide: !signalDetectionsColumnsToDisplay.get(SignalDetectionColumn.channel),
    width: medCellWidthPx
  },
  {
    headerName: signalDetectionColumnDisplayStrings.get(SignalDetectionColumn.phase),
    field: SignalDetectionColumn.phase,
    headerTooltip: signalDetectionColumnDisplayStrings.get(SignalDetectionColumn.phase),
    hide: !signalDetectionsColumnsToDisplay.get(SignalDetectionColumn.phase),
    comparator: caseInsensitiveComparator
  },
  {
    headerName: signalDetectionColumnDisplayStrings.get(SignalDetectionColumn.phaseConfidence),
    field: SignalDetectionColumn.phaseConfidence,
    headerTooltip: signalDetectionColumnDisplayStrings.get(SignalDetectionColumn.phaseConfidence),
    hide: !signalDetectionsColumnsToDisplay.get(SignalDetectionColumn.phaseConfidence)
  },
  {
    headerName: signalDetectionColumnDisplayStrings.get(SignalDetectionColumn.time),
    field: SignalDetectionColumn.time,
    headerTooltip: 'Arrival time',
    hide: !signalDetectionsColumnsToDisplay.get(SignalDetectionColumn.time),
    width: largeCellWidthPx,
    initialSort: 'asc'
  },
  {
    headerName: signalDetectionColumnDisplayStrings.get(
      SignalDetectionColumn.timeStandardDeviation
    ),
    field: SignalDetectionColumn.timeStandardDeviation,
    headerTooltip: 'Arrival time standard deviation',
    hide: !signalDetectionsColumnsToDisplay.get(SignalDetectionColumn.timeStandardDeviation)
  },
  {
    headerName: signalDetectionColumnDisplayStrings.get(SignalDetectionColumn.azimuth),
    field: SignalDetectionColumn.azimuth,
    headerTooltip: 'Observed receiver-to-source azimuth',
    hide: !signalDetectionsColumnsToDisplay.get(SignalDetectionColumn.azimuth),
    comparator: numericStringComparator
  },
  {
    headerName: signalDetectionColumnDisplayStrings.get(
      SignalDetectionColumn.azimuthStandardDeviation
    ),
    field: SignalDetectionColumn.azimuthStandardDeviation,
    headerTooltip: 'Azimuth standard deviation',
    hide: !signalDetectionsColumnsToDisplay.get(SignalDetectionColumn.azimuthStandardDeviation),
    comparator: numericStringComparator,
    width: standardDeviationHeaderWidthPx
  },
  {
    headerName: signalDetectionColumnDisplayStrings.get(SignalDetectionColumn.slowness),
    field: SignalDetectionColumn.slowness,
    headerTooltip: 'Observed slowness',
    hide: !signalDetectionsColumnsToDisplay.get(SignalDetectionColumn.slowness),
    comparator: numericStringComparator
  },
  {
    headerName: signalDetectionColumnDisplayStrings.get(
      SignalDetectionColumn.slownessStandardDeviation
    ),
    field: SignalDetectionColumn.slownessStandardDeviation,
    headerTooltip: 'Slowness standard deviation',
    hide: !signalDetectionsColumnsToDisplay.get(SignalDetectionColumn.slownessStandardDeviation),
    comparator: numericStringComparator,
    width: standardDeviationHeaderWidthPx
  },
  {
    headerName: signalDetectionColumnDisplayStrings.get(SignalDetectionColumn.amplitude),
    field: SignalDetectionColumn.amplitude,
    headerTooltip: signalDetectionColumnDisplayStrings.get(SignalDetectionColumn.amplitude),
    hide: !signalDetectionsColumnsToDisplay.get(SignalDetectionColumn.amplitude),
    comparator: numericStringComparator
  },
  {
    headerName: signalDetectionColumnDisplayStrings.get(SignalDetectionColumn.period),
    field: SignalDetectionColumn.period,
    headerTooltip: signalDetectionColumnDisplayStrings.get(SignalDetectionColumn.period),
    hide: !signalDetectionsColumnsToDisplay.get(SignalDetectionColumn.period),
    comparator: numericStringComparator
  },
  {
    headerName: signalDetectionColumnDisplayStrings.get(SignalDetectionColumn.sNR),
    field: SignalDetectionColumn.sNR,
    headerTooltip: 'Arrival time signal to noise ratio',
    hide: !signalDetectionsColumnsToDisplay.get(SignalDetectionColumn.sNR),
    comparator: numericStringComparator
  },
  {
    headerName: signalDetectionColumnDisplayStrings.get(SignalDetectionColumn.rectilinearity),
    field: SignalDetectionColumn.rectilinearity,
    headerTooltip: 'Signal rectilinearity',
    hide: !signalDetectionsColumnsToDisplay.get(SignalDetectionColumn.rectilinearity),
    comparator: numericStringComparator
  },
  {
    headerName: signalDetectionColumnDisplayStrings.get(SignalDetectionColumn.emergenceAngle),
    field: SignalDetectionColumn.emergenceAngle,
    headerTooltip: signalDetectionColumnDisplayStrings.get(SignalDetectionColumn.emergenceAngle),
    hide: !signalDetectionsColumnsToDisplay.get(SignalDetectionColumn.emergenceAngle),
    comparator: numericStringComparator
  },
  {
    headerName: signalDetectionColumnDisplayStrings.get(
      SignalDetectionColumn.shortPeriodFirstMotion
    ),
    field: SignalDetectionColumn.shortPeriodFirstMotion,
    headerTooltip: signalDetectionColumnDisplayStrings.get(
      SignalDetectionColumn.shortPeriodFirstMotion
    ),
    hide: !signalDetectionsColumnsToDisplay.get(SignalDetectionColumn.shortPeriodFirstMotion),
    width: medCellWidthPx
  },
  {
    headerName: signalDetectionColumnDisplayStrings.get(
      SignalDetectionColumn.longPeriodFirstMotion
    ),
    field: SignalDetectionColumn.longPeriodFirstMotion,
    headerTooltip: signalDetectionColumnDisplayStrings.get(
      SignalDetectionColumn.longPeriodFirstMotion
    ),
    hide: !signalDetectionsColumnsToDisplay.get(SignalDetectionColumn.longPeriodFirstMotion),
    width: medCellWidthPx
  },
  {
    headerName: signalDetectionColumnDisplayStrings.get(SignalDetectionColumn.rejected),
    field: SignalDetectionColumn.rejected,
    headerTooltip: signalDetectionColumnDisplayStrings.get(SignalDetectionColumn.rejected),
    hide: !signalDetectionsColumnsToDisplay.get(SignalDetectionColumn.rejected)
  }
];
