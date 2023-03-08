import { materialRenderers } from '@jsonforms/material-renderers';
import RatingControl from '../RatingControl';
import ratingControlTester from '../ratingControlTester';
import ChannelChecklistControl from './channel-checklist/ChannelChecklistControl';
import channelChecklistControlTester from './channel-checklist/channelChecklistControlTester';
import DirectoryPathInputControl from './directory-path-input/DirectoryPathInputControl';
import directoryPathInputControlTester from './directory-path-input/directoryPathInputControlTester';
import FileUploadControl from './file-input/FileUploadControl';
import fileUploadControlTester from './file-input/fileUploadControlTester';
import FilePathInputControl from './file-path-input/FilePathInputControl';
import filePathInputControlTester from './file-path-input/filePathInputControlTester';
import ISOTimeInputControl from './iso-time-input/ISOTimeInputControl';
import isoTimeInputControlTester from './iso-time-input/isoTimeInputControlTester';
import MarkdownControl from './markdown/MarkdownControl';
import markdownControlTester from './markdown/markdownControlTester';
import MonitorTypesRollupControl from './monitor-types-rollup/MonitorTypesRollupControl';
import monitorTypesRollupControlTester from './monitor-types-rollup/monitorTypesRollupControlTester';
import StationDurationsControl from './station-durations/StationDurationsControl';
import stationDurationsTester from './station-durations/stationDurationsTester';
import StationGroupsChecklistControl from './station-groups-checklist/StationGroupsChecklistControl';
import stationGroupsChecklistControlTester from './station-groups-checklist/stationGroupsChecklistControlTester';
import StationNameControl from './station-name/StationNameControl';
import stationNameControlTester from './station-name/stationNameControlTester';
import URLInputControl from './url-input/URLInputControl';
import urlInputControlTester from './url-input/urlInputControlTester';

export const renderers = [
  ...materialRenderers,
  //register custom renderers
  { tester: ratingControlTester, renderer: RatingControl },
  { tester: fileUploadControlTester, renderer: FileUploadControl },
  { tester: filePathInputControlTester, renderer: FilePathInputControl },
  {
    tester: directoryPathInputControlTester,
    renderer: DirectoryPathInputControl,
  },
  { tester: stationNameControlTester, renderer: StationNameControl },
  { tester: markdownControlTester, renderer: MarkdownControl },
  { tester: urlInputControlTester, renderer: URLInputControl },
  { tester: channelChecklistControlTester, renderer: ChannelChecklistControl },
  {
    tester: monitorTypesRollupControlTester,
    renderer: MonitorTypesRollupControl,
  },
  {
    tester: isoTimeInputControlTester,
    renderer: ISOTimeInputControl,
  },
  {
    tester: stationDurationsTester,
    renderer: StationDurationsControl,
  },
  {
    tester: stationGroupsChecklistControlTester,
    renderer: StationGroupsChecklistControl,
  },
];
