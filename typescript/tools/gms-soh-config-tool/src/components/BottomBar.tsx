import { Grid, Paper } from '@mui/material';
import { makeStyles } from '@mui/styles';
import React from 'react';
import { layoutStyles } from '../styles/layout';

const useStyles = makeStyles({
  ...layoutStyles,
  bottomBarContainer: {
    position: 'fixed',
    width: '100vw',
    left: 0,
    bottom: 0,
    zIndex: 10,
  },
});

/**
 * The type of the props for the {@link BottomBar} component
 */
export interface BottomBarProps {}

/**
 * This creates a fixed bottom bar with the provided children within
 */
export const BottomBar: React.FC<React.PropsWithChildren<BottomBarProps>> = ({
  children,
}: React.PropsWithChildren<BottomBarProps>) => {
  const classes = useStyles();
  return (
    <Paper className={classes.bottomBarContainer} elevation={3}>
      <Grid
        container
        justifyContent={'center'}
        spacing={1}
        className={classes.container}
      >
        {children}
      </Grid>
    </Paper>
  );
};
