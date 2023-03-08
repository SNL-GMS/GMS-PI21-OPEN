import type { SignalDetectionTypes } from '@gms/common-model';
import { LegacyEventTypes } from '@gms/common-model';
import { setDecimalPrecision } from '@gms/common-util';
import type { ColumnGroupDefinition } from '@gms/ui-core-components';
import type { AnalystWorkspaceTypes } from '@gms/ui-state';

import {
  MagDefiningStates,
  magnitudeColumnRepeatedArguments
} from '~analyst-ui/components/magnitude/types';
import { systemConfig } from '~analyst-ui/config';
import { messageConfig } from '~analyst-ui/config/message-config';
import { gmsColors } from '~scss-config/color-preferences';

import { MagDefiningCheckBoxCellRenderer, ToolTipRenderer } from './cell-renderer-frameworks';
import { DefiningHeader } from './header-group-renderer-frameworks';

const historicalColor = gmsColors.gmsSoft;
const highlightColorOdd = gmsColors.gmsTableHighlightOddRow;
const highlightColorEven = gmsColors.gmsTableHighlightEvenRow;

// Apply a highlight to the selected cell. Will color it conditionally based on whether it is selected or not.
const applyHighlightToCell = (params: any): any => {
  const { magnitudeType } = params.colDef;
  // eslint-disable-next-line @typescript-eslint/no-use-before-define
  const signalDetectionId = getValueFromParams(params, magnitudeType, 'signalDetectionId');
  const isSelected = params.data.selectedSdIds.find(sdId => sdId === signalDetectionId);
  // eslint-disable-next-line @typescript-eslint/no-use-before-define
  const magTypeHasError = getValueFromParams(params, magnitudeType, 'hasMagnitudeCalculationError');
  return {
    'text-align': 'right',
    'background-color':
      // eslint-disable-next-line no-nested-ternary
      params.node.rowIndex % 2 === 0
        ? // eslint-disable-next-line no-nested-ternary
          isSelected
          ? magTypeHasError
            ? gmsColors.gmsTableWarningSelected
            : gmsColors.gmsTableHighlightSelected
          : magTypeHasError
          ? gmsColors.gmsTableWarningEvenRow
          : highlightColorEven
        : // eslint-disable-next-line no-nested-ternary
        isSelected
        ? magTypeHasError
          ? gmsColors.gmsTableWarningSelected
          : gmsColors.gmsTableHighlightSelected
        : magTypeHasError
        ? gmsColors.gmsTableWarningOddRow
        : highlightColorOdd
  };
};

const applyHistoricalToHeader = params => ({
  'text-align': 'right',
  color: params.data.historicalMode ? historicalColor : undefined
});

/**
 * Gets data from the row parameters based off of magnitude type and access key
 *
 * @param params ag grid params
 * @param magnitudeType mag type to get data for
 * @param key key of the data in the object
 */
function getValueFromParams(params, magnitudeType: LegacyEventTypes.MagnitudeType, key: string) {
  return params.data.dataForMagnitude.get(magnitudeType)[key];
}

function generateMagnitudeColumn(
  magnitudeType: LegacyEventTypes.MagnitudeType,
  amplitudeType: SignalDetectionTypes.AmplitudeType,
  defStates: MagDefiningStates,
  displayedMagnitudeTypes: AnalystWorkspaceTypes.DisplayedMagnitudeTypes,
  stationIds: string[],
  definingCallback: (
    magnitudeType: LegacyEventTypes.MagnitudeType,
    stationIds: string[],
    defining: boolean
  ) => void
) {
  const hasNoneChecked = defStates === MagDefiningStates.NONE;
  return {
    headerName: magnitudeType,
    headerGroupComponentFramework: DefiningHeader,
    headerGroupComponentParams: {
      definingState: defStates,
      callback: definingCallback,
      magnitudeType,
      stationIds
    },
    children: [
      {
        headerName: 'Channel',
        valueGetter: params => getValueFromParams(params, magnitudeType, 'channel'),
        hide: !displayedMagnitudeTypes[magnitudeType],
        magnitudeType,
        cellStyle: params => ({
          'text-align': 'left',
          color: params.data.historicalMode ? historicalColor : undefined,
          'background-color':
            !params.data.historicalMode &&
            params.data.selectedSdIds.find(
              sdId => sdId === getValueFromParams(params, magnitudeType, 'signalDetectionId')
            )
              ? gmsColors.gmsTableSelection
              : 'auto',
          'padding-left': '2px'
        }),
        width: 71,
        ...magnitudeColumnRepeatedArguments
      },
      {
        headerName: 'Phase',
        magnitudeType,
        valueGetter: params => getValueFromParams(params, magnitudeType, 'phase'),
        hide: !displayedMagnitudeTypes[magnitudeType],
        cellStyle: params => ({
          'text-align': 'left',
          color: params.data.historicalMode ? historicalColor : undefined,
          'background-color':
            !params.data.historicalMode &&
            params.data.selectedSdIds.find(
              sdId => sdId === getValueFromParams(params, magnitudeType, 'signalDetectionId')
            )
              ? gmsColors.gmsTableSelection
              : 'auto'
        }),
        width: 60,
        ...magnitudeColumnRepeatedArguments
      },
      {
        headerName: systemConfig.amplitudeTypeToDisplayName.get(amplitudeType),
        magnitudeType,
        valueGetter: params => getValueFromParams(params, magnitudeType, 'amplitudeValue'),
        hide: !displayedMagnitudeTypes[magnitudeType],
        cellStyle: params => {
          const flagForReview = getValueFromParams(params, magnitudeType, 'flagForReview');
          const signalDetectionId = getValueFromParams(params, magnitudeType, 'signalDetectionId');
          const isSelected = params.data.selectedSdIds.find(sdId => sdId === signalDetectionId);
          return {
            'text-align': 'right',
            // eslint-disable-next-line no-nested-ternary
            color: flagForReview
              ? gmsColors.gmsRecessed
              : params.data.historicalMode
              ? historicalColor
              : undefined,
            // eslint-disable-next-line no-nested-ternary
            'background-color': flagForReview
              ? // eslint-disable-next-line no-nested-ternary
                params.node.rowIndex % 2 === 0
                ? isSelected
                  ? gmsColors.gmsTableRequiresReviewSelected
                  : gmsColors.gmsTableRequiresReviewEvenRow
                : isSelected
                ? gmsColors.gmsTableRequiresReviewSelected
                : gmsColors.gmsTableRequiresReviewOddRow
              : !params.data.historicalMode && isSelected
              ? gmsColors.gmsTableSelection
              : 'auto'
          };
        },
        width: 60,
        ...magnitudeColumnRepeatedArguments,
        valueFormatter: params =>
          setDecimalPrecision(getValueFromParams(params, magnitudeType, 'amplitudeValue'))
      },
      {
        headerName: 'Period (s)',
        magnitudeType,
        valueGetter: params => getValueFromParams(params, magnitudeType, 'amplitudePeriod'),
        hide: !displayedMagnitudeTypes[magnitudeType],
        cellStyle: params => ({
          'text-align': 'right',
          color: params.data.historicalMode ? historicalColor : undefined,
          'background-color':
            !params.data.historicalMode &&
            params.data.selectedSdIds.find(
              sdId => sdId === getValueFromParams(params, magnitudeType, 'signalDetectionId')
            )
              ? gmsColors.gmsTableSelection
              : 'auto'
        }),
        width: 80,
        ...magnitudeColumnRepeatedArguments,
        valueFormatter: params =>
          setDecimalPrecision(getValueFromParams(params, magnitudeType, 'amplitudePeriod'))
      },
      {
        headerName: 'Def',
        magnitudeType,
        valueGetter: params => getValueFromParams(params, magnitudeType, 'defining'),
        hide: !displayedMagnitudeTypes[magnitudeType],
        cellStyle: (params: any) => ({
          display: 'flex',
          'justify-content': 'center',
          'padding-left': '9px',
          'padding-top': '3px',
          color: params.data.historicalMode ? historicalColor : undefined,
          'background-color':
            // eslint-disable-next-line no-nested-ternary
            !params.data.historicalMode &&
            params.data.selectedSdIds.find(
              sdId => sdId === getValueFromParams(params, magnitudeType, 'signalDetectionId')
            )
              ? hasNoneChecked
                ? gmsColors.gmsTableWarningSelected
                : gmsColors.gmsTableSelection
              : // eslint-disable-next-line no-nested-ternary
              hasNoneChecked
              ? params.node.rowIndex % 2 === 0
                ? gmsColors.gmsTableWarningOddRow
                : gmsColors.gmsTableWarningEvenRow
              : 'auto'
        }),
        cellRendererFramework: MagDefiningCheckBoxCellRenderer,
        width: 50,
        ...magnitudeColumnRepeatedArguments,
        cellRendererParams: {
          padding: 25,
          magnitudeType,
          tooltip: hasNoneChecked
            ? messageConfig.tooltipMessages.magnitude.noStationsSetToDefiningMessage
            : undefined
        }
      },
      {
        headerName: 'Mag',
        magnitudeType,
        valueGetter: params => getValueFromParams(params, magnitudeType, 'mag'),
        cellStyle: applyHighlightToCell,
        width: 60,
        hide: !displayedMagnitudeTypes[magnitudeType],
        ...magnitudeColumnRepeatedArguments,
        valueFormatter: params =>
          setDecimalPrecision(getValueFromParams(params, magnitudeType, 'mag'), 2),
        cellRendererFramework: ToolTipRenderer,
        cellRendererParams: params => ({
          tooltip: getValueFromParams(params, magnitudeType, 'hasMagnitudeCalculationError')
            ? getValueFromParams(params, magnitudeType, 'computeNetworkMagnitudeSolutionStatus')
            : undefined
        })
      },
      {
        headerName: 'Res',
        magnitudeType,
        valueGetter: params => getValueFromParams(params, magnitudeType, 'res'),
        hide: !displayedMagnitudeTypes[magnitudeType],
        cellStyle: applyHighlightToCell,
        width: 60,
        ...magnitudeColumnRepeatedArguments,
        cellRendererFramework: ToolTipRenderer,
        cellRendererParams: params => ({
          tooltip: getValueFromParams(params, magnitudeType, 'hasMagnitudeCalculationError')
            ? getValueFromParams(params, magnitudeType, 'computeNetworkMagnitudeSolutionStatus')
            : undefined
        }),
        valueFormatter: params =>
          setDecimalPrecision(getValueFromParams(params, magnitudeType, 'res'), 2)
      }
    ]
  };
}

/**
 * Generates column defs
 */
export function generateStationMagnitudeColumnDefs(
  defStatesForMagnitudeType: Map<LegacyEventTypes.MagnitudeType, MagDefiningStates>,
  stationIdsForMagnitudeType: Map<LegacyEventTypes.MagnitudeType, string[]>,
  displayedMagnitudeTypes: AnalystWorkspaceTypes.DisplayedMagnitudeTypes,
  historicalMode: boolean,
  definingCallback: (
    magnitudeType: LegacyEventTypes.MagnitudeType,
    stationIds: string[],
    defining: boolean
  ) => void
): ColumnGroupDefinition[] {
  const pinnedColumns = {
    headerName: historicalMode ? 'Station magnitude (readonly)' : 'Station magnitude',
    children: [
      {
        headerName: 'Station',
        field: 'station',
        cellStyle: () => ({ 'text-align': 'left' }),
        width: 70,
        ...magnitudeColumnRepeatedArguments,
        pinned: true
      },
      {
        headerName: 'Dist (\u00B0)',
        field: 'dist',
        cellStyle: applyHistoricalToHeader,
        width: 80,
        ...magnitudeColumnRepeatedArguments,
        pinned: true,
        valueFormatter: e => setDecimalPrecision(e.data.dist)
      },
      {
        headerName: 'S-R Azimuth (\u00B0)',
        field: 'azimuth',
        cellStyle: applyHistoricalToHeader,
        width: 120,
        tooltipValueGetter: () =>
          messageConfig.tooltipMessages.magnitude.sourceToReceiverAzimuthMessage,
        pinned: true,
        ...magnitudeColumnRepeatedArguments,
        valueFormatter: e => setDecimalPrecision(e.data.azimuth)
      }
    ]
  };
  const magnitudeHeaderGroups: ColumnGroupDefinition[] = Object.keys(
    LegacyEventTypes.MagnitudeType
  ).map(key =>
    generateMagnitudeColumn(
      LegacyEventTypes.MagnitudeType[key],
      systemConfig.amplitudeTypeForMagnitude.get(LegacyEventTypes.MagnitudeType[key]),
      defStatesForMagnitudeType.get(LegacyEventTypes.MagnitudeType[key]),
      displayedMagnitudeTypes,
      stationIdsForMagnitudeType.get(LegacyEventTypes.MagnitudeType[key]),
      definingCallback
    )
  );

  const columnDefs: ColumnGroupDefinition[] = [pinnedColumns, ...magnitudeHeaderGroups];
  return columnDefs;
}
