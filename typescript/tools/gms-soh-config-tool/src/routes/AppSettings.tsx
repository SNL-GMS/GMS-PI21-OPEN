import { materialCells } from '@jsonforms/material-renderers';
import { JsonForms } from '@jsonforms/react';
import { Grid, Typography } from '@mui/material';
import { makeStyles } from '@mui/styles';
import { isEqual } from 'lodash';
import React from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { renderers } from '../renderers';
import appSettingsSchema from '../schema/app-settings-schema.json';
import appSettingsUiSchema from '../schema/app-settings-ui-schema.json';
import { configApi } from '../state/api-slice';
import { AppSettingsState, setAppSettings } from '../state/app-settings-slice';
import {
  clearStationErrors,
  resetLoadedData,
  setStationName,
} from '../state/station-controls-slice';
import type { AppState } from '../state/store';
import { layoutStyles } from '../styles/layout';

const useStyles = makeStyles({
  ...layoutStyles,
});

/**
 * The page that is displayed when you visit the app settings
 */
export const AppSettings: React.FC<{}> = () => {
  const classes = useStyles();
  const settingsData = useSelector((state: AppState) => state['app-settings']);
  const dispatch = useDispatch();
  const updateAppSettings = React.useCallback(
    (settings: AppSettingsState) => {
      dispatch(setAppSettings(settings));
    },
    [dispatch]
  );

  React.useLayoutEffect(() => {
    dispatch(setStationName(null));
    dispatch(configApi.util.resetApiState()); // ensures a fresh query and not from cache
    dispatch(clearStationErrors());
    dispatch(resetLoadedData());
  }, [dispatch]);

  return (
    <Grid
      container
      justifyContent={'center'}
      spacing={1}
      className={classes.container}
    >
      <Grid item xs={12} sm={12} md={12} lg={8}>
        <Typography variant={'h4'} className={classes.title}>
          Settings
        </Typography>
      </Grid>
      <Grid item xs={12} justifyContent={'center'}>
        <Typography variant='body1' textAlign={'center'}>
          By default the tool is using the app resource directory for required
          files and output. All that is needed to be set is the url to a config
          deployment. File paths can be modified below, but will require
          structure identical to 'soh-configs' located in home/soh-configs
          directory. These settings will be automatically saved and loaded next
          time the app is opened.
        </Typography>
        <Typography variant='body1' textAlign={'center'}>
          --------
        </Typography>
        <Typography variant='body1' textAlign={'center'}>
          NOTE: When updating directory or files, window may appear behind
          application.
        </Typography>
        <Typography variant='body1' textAlign={'center'}>
          To load the new configuration, press Ctrl + R, or restart the
          application.
        </Typography>
      </Grid>
      <Grid item xs={12} sm={12} md={12} lg={8}>
        <JsonForms
          schema={appSettingsSchema}
          uischema={appSettingsUiSchema}
          data={settingsData}
          renderers={renderers}
          cells={materialCells}
          onChange={({ data: d }) => {
            if (!isEqual(d, settingsData)) updateAppSettings(d);
          }}
        />
      </Grid>
    </Grid>
  );
};
