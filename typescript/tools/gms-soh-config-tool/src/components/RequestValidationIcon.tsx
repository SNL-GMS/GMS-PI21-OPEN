import { makeStyles } from '@mui/styles';
import CheckCircleOutlineIcon from '@mui/icons-material/CheckCircleOutline';
import CloseIcon from '@mui/icons-material/Close';
import HourglassEmptyIcon from '@mui/icons-material/HourglassEmpty';

const useStyles = makeStyles({
  validationIcon: {
    position: 'absolute',
    left: '0',
    top: '50%',
    height: '50%',
    bottom: '0',
    paddingTop: '2px',
    transform: 'translate3d(-50%, -50%, 0)',
    borderRadius: '100%',
    background: 'white',
  },
  leftIconContainer: {
    position: 'absolute',
    left: 0,
  },
});

export interface RequestValidationIconProps {
  result: {
    data?: any;
    error?: any;
    isError?: boolean;
    isFetching?: boolean;
  };
}

/**
 * Shows a validation icon to the left side of an input. It must be in the same, relatively positioned parent
 * as the input, and it uses that parent container to position itself.
 * Shows an hourglass if it is fetching.
 * Shows a green checkbox if we got data that is truthy.
 * Shows a red X if none of the above.
 */
export const RequestValidationIcon: React.VFC<RequestValidationIconProps> = (
  props
) => {
  const classes = useStyles();
  if (props.result?.isFetching) {
    return <HourglassEmptyIcon className={classes.validationIcon} />;
  }
  if (props.result?.data) {
    return (
      <CheckCircleOutlineIcon
        className={classes.validationIcon}
        color='success'
      />
    );
  }
  return <CloseIcon className={classes.validationIcon} color='warning' />;
};
