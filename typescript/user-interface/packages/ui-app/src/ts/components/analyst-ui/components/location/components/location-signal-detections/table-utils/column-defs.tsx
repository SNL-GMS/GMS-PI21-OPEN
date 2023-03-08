import { setDecimalPrecision } from '@gms/common-util';
import type { ColumnGroupDefinition } from '@gms/ui-core-components';

import { getDiffStyleForDefining } from '~analyst-ui/common/utils/location-utils';
import type { DefiningStates } from '~analyst-ui/components/location/components/location-signal-detections/types';
import { DefiningTypes } from '~analyst-ui/components/location/components/location-signal-detections/types';
import { gmsColors } from '~scss-config/color-preferences';

import { AddedRemovedSDMarker, DefiningCheckBoxCellRenderer } from './cell-renderer-frameworks';
import { DefiningHeader } from './header-group-component';

export type DefiningCallback = (isDefining: boolean, definingType: DefiningTypes) => void;
/**
 * Generates column defs
 *
 * @param definingCallback callback to set all sd's defining status
 */
export function generateLocationSDColumnDef(
  definingCallback: DefiningCallback,
  timeDefiningState: DefiningStates,
  aziumthDefiningState: DefiningStates,
  slownessDefiningState: DefiningStates,
  historicalMode: boolean
): ColumnGroupDefinition[] {
  const columnDefs: ColumnGroupDefinition[] = [
    {
      headerName: historicalMode
        ? 'Associated Signal Detections (readonly)'
        : 'Associated Signal Detections',
      children: [
        {
          headerName: '',
          field: 'modified',
          cellStyle: () => ({
            display: 'flex',
            'justify-content': 'center',
            'align-items': 'center'
          }),
          width: 20,
          resizable: true,
          sortable: true,
          filter: true,
          cellRendererFramework: AddedRemovedSDMarker
        },
        {
          headerName: 'Station',
          field: 'station',
          width: 70,
          cellStyle: (params: any) => ({
            'text-align': 'left',
            color:
              historicalMode || params.data.rejectedOrUnassociated ? gmsColors.gmsSoft : undefined
          }),
          resizable: true,
          sortable: true,
          filter: true,
          suppressMovable: true
        },
        {
          headerName: 'Channel',
          field: 'channel',
          width: 71,
          cellStyle: (params: any) => ({
            'text-align': 'left',
            'background-color': params.data.channelNameDiff ? gmsColors.gmsTableChangeMarker : '',
            color:
              historicalMode || params.data.rejectedOrUnassociated ? gmsColors.gmsSoft : undefined
          }),
          resizable: true,
          sortable: true,
          filter: true,
          suppressMovable: true
        },
        {
          headerName: 'Phase',
          field: 'phase',
          width: 70,
          cellStyle: (params: any) => ({
            'text-align': 'left',
            'background-color': params.data.phaseDiff ? gmsColors.gmsTableChangeMarker : '',
            color:
              historicalMode || params.data.rejectedOrUnassociated ? gmsColors.gmsSoft : undefined
          }),
          resizable: true,
          sortable: true,
          filter: true,
          suppressMovable: true
        },
        {
          headerName: 'Dist (\u00B0)',
          field: 'distance',
          width: 70,
          cellStyle: (params: any) => ({
            'text-align': 'right',
            'border-right': '1px white #182026',
            color:
              historicalMode || params.data.rejectedOrUnassociated ? gmsColors.gmsSoft : undefined
          }),
          valueFormatter: e => setDecimalPrecision(e.data.distance),
          resizable: true,
          sortable: true,
          filter: true,
          suppressMovable: true
        }
      ]
    },
    {
      headerName: 'Time',
      headerGroupComponentFramework: DefiningHeader,
      headerGroupComponentParams: {
        definingCallback,
        definingType: DefiningTypes.ARRIVAL_TIME,
        definingState: timeDefiningState
      },
      children: [
        {
          headerName: 'Def',
          field: 'timeDefnCB',
          width: 50,
          resizable: true,
          sortable: true,
          filter: true,
          suppressMovable: true,
          cellRendererFramework: DefiningCheckBoxCellRenderer,
          cellStyle: (params: any) => ({
            display: 'flex',
            'justify-content': 'center',
            'padding-left': '9px',
            'padding-top': '3px',
            ...getDiffStyleForDefining(DefiningTypes.ARRIVAL_TIME, params.data)
          }),
          cellRendererParams: {
            padding: 25,
            definingType: DefiningTypes.ARRIVAL_TIME
          }
        },
        {
          headerName: 'Obs',
          field: 'timeObs',
          width: 210,
          cellStyle: (params: any) => ({
            'text-align': 'right',
            'background-color': params.data.arrivalTimeDiff ? gmsColors.gmsTableChangeMarker : '',
            color:
              historicalMode || params.data.rejectedOrUnassociated ? gmsColors.gmsSoft : undefined
          }),
          resizable: true,
          sortable: true,
          filter: true,
          suppressMovable: true
        },
        {
          headerName: 'Res (s)',
          field: 'timeRes',
          width: 70,
          cellStyle: (params: any) => ({
            'text-align': 'right',
            color:
              historicalMode || params.data.rejectedOrUnassociated ? gmsColors.gmsSoft : undefined
          }),
          valueFormatter: e => setDecimalPrecision(e.data.timeRes, 2),
          resizable: true,
          sortable: true,
          filter: true,
          suppressMovable: true
        },
        {
          headerName: 'Corr (s)',
          field: 'timeCorr',
          width: 70,
          cellStyle: (params: any) => ({
            'text-align': 'right',
            'border-right': '1px dotted #182026',
            color:
              historicalMode || params.data.rejectedOrUnassociated ? gmsColors.gmsSoft : undefined
          }),
          valueFormatter: e => setDecimalPrecision(e.data.timeCorr),
          resizable: true,
          sortable: true,
          filter: true,
          suppressMovable: true
        }
      ]
    },
    {
      headerName: 'Slowness',
      headerGroupComponentFramework: DefiningHeader,
      headerGroupComponentParams: {
        definingCallback,
        definingType: DefiningTypes.SLOWNESS,
        definingState: slownessDefiningState
      },
      children: [
        {
          headerName: 'Def',
          field: 'slownessDefnCB',
          width: 50,
          resizable: true,
          sortable: true,
          filter: true,
          suppressMovable: true,
          cellStyle: (params: any) => ({
            display: 'flex',
            'justify-content': 'center',
            'padding-left': '9px',
            'padding-top': '3px',
            ...getDiffStyleForDefining(DefiningTypes.SLOWNESS, params.data)
          }),
          cellRendererFramework: DefiningCheckBoxCellRenderer,
          cellRendererParams: {
            padding: 25,
            definingType: DefiningTypes.SLOWNESS
          }
        },
        {
          headerName: 'Obs (s/\u00B0)',
          field: 'slownessObs',
          width: 75,
          cellStyle: (params: any) => ({
            'text-align': 'right',
            'background-color': params.data.slownessObsDiff ? gmsColors.gmsTableChangeMarker : '',
            color:
              historicalMode || params.data.rejectedOrUnassociated ? gmsColors.gmsSoft : undefined
          }),
          valueFormatter: e => setDecimalPrecision(e.data.slownessObs),
          resizable: true,
          sortable: true,
          filter: true,
          suppressMovable: true
        },
        {
          headerName: 'Res (s/\u00B0)',
          field: 'slownessRes',
          width: 70,
          cellStyle: (params: any) => ({
            'text-align': 'right',
            color:
              historicalMode || params.data.rejectedOrUnassociated ? gmsColors.gmsSoft : undefined
          }),
          valueFormatter: e => setDecimalPrecision(e.data.slownessRes, 2),
          resizable: true,
          sortable: true,
          filter: true,
          suppressMovable: true
        },
        {
          headerName: 'Corr (s/\u00B0)',
          field: 'slownessCorr',
          width: 75,
          cellStyle: (params: any) => ({
            'text-align': 'right',
            color:
              historicalMode || params.data.rejectedOrUnassociated ? gmsColors.gmsSoft : undefined,
            'border-right': '1px #182026 dotted'
          }),
          valueFormatter: e => setDecimalPrecision(e.data.slownessCorr),
          resizable: true,
          sortable: true,
          filter: true,
          suppressMovable: true
        }
      ]
    },
    {
      headerName: 'Azimuth',
      headerGroupComponentFramework: DefiningHeader,
      headerGroupComponentParams: {
        definingCallback,
        definingType: DefiningTypes.AZIMUTH,
        definingState: aziumthDefiningState
      },
      children: [
        {
          headerName: 'Def',
          field: 'azimuthDefnCB',
          width: 50,
          resizable: true,
          sortable: true,
          filter: true,
          suppressMovable: true,
          cellStyle: (params: any) => ({
            display: 'flex',
            'justify-content': 'center',
            'padding-left': '9px',
            'padding-top': '3px',
            ...getDiffStyleForDefining(DefiningTypes.AZIMUTH, params.data)
          }),
          cellRendererFramework: DefiningCheckBoxCellRenderer,
          cellRendererParams: {
            padding: 25,
            definingType: DefiningTypes.AZIMUTH
          }
        },
        {
          headerName: 'Obs (\u00B0)',
          field: 'azimuthObs',
          width: 70,
          cellStyle: (params: any) => ({
            'text-align': 'right',
            'background-color': params.data.azimuthObsDiff ? gmsColors.gmsTableChangeMarker : '',
            color:
              historicalMode || params.data.rejectedOrUnassociated ? gmsColors.gmsSoft : undefined
          }),
          valueFormatter: e => setDecimalPrecision(e.data.azimuthObs),
          resizable: true,
          sortable: true,
          filter: true,
          suppressMovable: true
        },
        {
          headerName: 'Res (\u00B0)',
          field: 'azimuthRes',
          width: 70,
          cellStyle: (params: any) => ({
            'text-align': 'right',
            color:
              historicalMode || params.data.rejectedOrUnassociated ? gmsColors.gmsSoft : undefined
          }),
          valueFormatter: e => setDecimalPrecision(e.data.azimuthRes, 2),
          resizable: true,
          sortable: true,
          filter: true,
          suppressMovable: true
        },
        {
          headerName: 'Corr (\u00B0)',
          field: 'azimuthCorr',
          width: 70,
          cellStyle: (params: any) => ({
            'text-align': 'right',
            color:
              historicalMode || params.data.rejectedOrUnassociated ? gmsColors.gmsSoft : undefined,
            'border-right': '1px #182026 dotted'
          }),
          valueFormatter: e => setDecimalPrecision(e.data.azimuthCorr),
          resizable: true,
          sortable: true,
          filter: true,
          suppressMovable: true
        }
      ]
    }
  ];
  return columnDefs;
}
