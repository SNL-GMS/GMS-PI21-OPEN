import { Grid, Typography } from '@mui/material';
import { makeStyles } from '@mui/styles';
import React from 'react';
import {
  isLoading,
  LoadingNonIdealState,
} from '../components/LoadingNonIdealState';
import { RawDataView } from '../components/RawDataView';
import { useAppContext } from '../state/state';
import { layoutStyles } from '../styles/layout';

const useStyles = makeStyles({
  ...layoutStyles,
});

/**
 * RawData component shows the current, internal state of the app. This is useful for debugging.
 * It may have use for the user, too, though it is likely not the primary way the user will get this info.
 */
export const RawData: React.FC = () => {
  const classes = useStyles();
  const { data, loadingState } = useAppContext();
  if (isLoading(loadingState)) {
    return <LoadingNonIdealState loadingState={loadingState} />;
  }
  return (
    <Grid
      container
      justifyContent={'center'}
      spacing={1}
      className={classes.container}
    >
      <Grid item xs={12} sm={12} md={12} lg={8}>
        <Typography variant={'h4'} className={classes.title}>
          Raw data
        </Typography>
        <RawDataView data={data} />
      </Grid>
    </Grid>
  );
};
