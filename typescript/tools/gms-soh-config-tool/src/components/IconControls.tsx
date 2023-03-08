import makeStyles from '@mui/styles/makeStyles';
import React from 'react';
import { layoutStyles } from '../styles/layout';

interface IconControlsProps {
  helpText?: string | undefined;
  isError?: boolean;
  controls: React.ReactNode[];
}

const useStyles = makeStyles({
  ...layoutStyles,
  controls: {
    position: 'absolute',
    width: '10em',
    left: '5em',
    top: -5,
    transform: 'translate3d(-50%, -50%, 0)',
    borderRadius: '100%',
    background: 'white',
    zIndex: 9,
    display: 'none',
  },
  wrapper: {
    position: 'relative',
    margin: 0,
    padding: 0,
    '&:hover $controls': {
      display: 'block',
    },
  },
});

/**
 * Renders an array onf icons
 * that shows on hover
 * @returns icons
 */
export const IconControls: React.FC<
  React.PropsWithChildren<IconControlsProps>
> = (props: React.PropsWithChildren<IconControlsProps>) => {
  const { children, controls } = props;
  const classes = useStyles();
  return (
    <div className={classes.wrapper}>
      <div className={classes.controls}>{controls}</div>
      {children ?? <></>}
    </div>
  );
};
