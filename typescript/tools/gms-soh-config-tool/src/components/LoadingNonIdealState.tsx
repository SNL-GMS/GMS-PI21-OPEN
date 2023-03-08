import { CircularProgress, Typography } from '@mui/material';
import { makeStyles } from '@mui/styles';
import React from 'react';
import type { LoadingState } from '../state/state';

const useStyles = makeStyles({
  nonIdealState: {
    margin: 'auto',
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    flexDirection: 'column',
    height: '100%',
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
  },
  nonIdealStateText: {
    fontSize: '2em',
    fontWeight: 'bold',
    paddingTop: '1em',
  },
  nonIdealStateValue: {
    padding: '.25em',
  },
  nonIdealStateLoading: {
    color: 'tomato',
  },
  nonIdealStateLoaded: {
    color: 'green',
  },
  description: {
    fontSize: '1.5em',
  },
});

export const isLoading = (loadingState: LoadingState) =>
  loadingState != null &&
  loadingState.numRequested - loadingState.numComplete > 0;

/**
 * The type of the props for the {@link LoadingNonIdealState} component
 */
export interface LoadingNonIdealStateProps {
  loadingState: LoadingState;
}

/**
 * Describe what this component does.
 * TypeDoc should contain more info than is already present
 * in the name and type signature of the component.
 */
export const LoadingNonIdealState: React.FC<LoadingNonIdealStateProps> = ({
  loadingState,
}: LoadingNonIdealStateProps) => {
  const classes = useStyles();
  return (
    <section className={classes.nonIdealState}>
      <CircularProgress size={'4rem'} />
      <Typography variant={'h4'} className={classes.nonIdealStateText}>
        <span
          className={`${classes.nonIdealStateValue} ${classes.nonIdealStateLoaded}`}
        >
          {loadingState.numComplete}
        </span>
        of
        <span
          className={`${classes.nonIdealStateValue} ${classes.nonIdealStateLoading}`}
        >
          {loadingState.numRequested}
        </span>
      </Typography>
      <Typography variant={'body1'} className={classes.description}>
        Loading configuration files
      </Typography>
    </section>
  );
};
