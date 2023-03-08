import EditIcon from '@mui/icons-material/Edit';
import ErrorIcon from '@mui/icons-material/Error';
import React from 'react';
import Slide from '@mui/material/Slide';
import makeStyles from '@mui/styles/makeStyles';
import { TransitionProps } from '@mui/material/transitions';
import Typography from '@mui/material/Typography';
import Dialog from '@mui/material/Dialog';
import AppBar from '@mui/material/AppBar';
import Toolbar from '@mui/material/Toolbar';
import IconButton from '@mui/material/IconButton';
import CloseIcon from '@mui/icons-material/Close';
import { Rollup } from './Rollup';
import { useAppSelector } from '../../state/react-redux-hooks';
import {
  OperatorType,
  RollupEntry,
  RollupType,
} from '../../state/station-controls-slice';
import { useAppContext } from '../../state/state';
import { determineSectionContainsErrorState } from '../../util/util';
import remarkGfm from 'remark-gfm';
import Tooltip from '@mui/material/Tooltip';
import ReactMarkdown from 'react-markdown';
import { AppSections } from '../../routes/types';

/**
 * The type of the props for the {@link StationCapabilityRollup} component
 */
export interface ChannelCapabilityRollupProps {
  groupName: string;
  channelName: string;
}

const useStyles = makeStyles({
  rollupContainer: {
    display: 'flex',
  },
  editIcon: {
    cursor: 'pointer',
  },
  addIcon: {
    marginLeft: '1.5em',
    cursor: 'pointer',
  },
  errorIcon: {
    color: 'tomato',
  },
});

const Transition = React.forwardRef(function Transition(
  props: TransitionProps & {
    children: React.ReactElement;
  },
  ref: React.Ref<unknown>
) {
  return <Slide direction='up' ref={ref} {...props} />;
});

/**
 * Creates a popover for station capability rollup.
 */
export const ChannelCapabilityRollup: React.FC<
  ChannelCapabilityRollupProps
> = ({ channelName, groupName }: ChannelCapabilityRollupProps) => {
  const { data } = useAppContext();
  const classes = useStyles();
  const stationName = useAppSelector(
    (state) => state.stationControls.stationName
  );
  const rollup = useAppSelector(
    (state) =>
      state.stationControls.channelCapabilityRollup[stationName ?? ''][
        groupName
      ][channelName]
  );
  const errors = useAppSelector((state) => state.stationControls.error);
  const [showPopover, setPopOver] = React.useState(false);
  const showStationCapabilityRollup = () => {
    setPopOver(true);
  };
  const allMonitorNames = data?.supportedMonitorTypes;

  // Uses only these specific options
  const rollupTypeOptions = [
    RollupType.ROLLUP_OF_ROLLUPS,
    RollupType.ROLLUP_OF_MONITORS,
  ];

  const determineRollupStatus = (rollup: RollupEntry): string => {
    if (rollup.rollupType === RollupType.ROLLUP_OF_ROLLUPS) {
      return `${rollup.operatorType} ROLLUPS`;
    }
    if (rollup.monitors) {
      if (rollup.monitors.length === allMonitorNames.length) {
        return `${rollup.operatorType} ALL_MONITORS`;
      }
      return `${rollup.operatorType} SELECTED_MONITORS`;
    }
    return '';
  };

  // Uses all the operator types
  const operatorTypeOptions = Object.keys(OperatorType).filter((item) => {
    return isNaN(Number(item));
  });

  const hasErrors =
    determineSectionContainsErrorState(
      errors,
      `${AppSections.CHANNEL_CAPABILITY} ${channelName}`
    ).length > 0;

  return (
    <div className={classes.rollupContainer}>
      <Typography sx={{ ml: 2, flex: 1 }} component='div'>
        {`(${determineRollupStatus(rollup)})`}
      </Typography>
      <EditIcon
        onClick={() => showStationCapabilityRollup()}
        className={classes.editIcon}
      />
      {hasErrors ? (
        <Tooltip
          title={
            <ReactMarkdown remarkPlugins={[remarkGfm]}>
              {`Capability Error`}
            </ReactMarkdown>
          }
        >
          <ErrorIcon className={classes.errorIcon} />
        </Tooltip>
      ) : undefined}
      <Dialog
        fullScreen
        open={showPopover}
        onClose={() => setPopOver(false)}
        TransitionComponent={Transition}
      >
        <AppBar sx={{ position: 'relative' }}>
          <Toolbar>
            <Typography sx={{ ml: 2, flex: 1 }} variant='h6' component='div'>
              {`${channelName}: Channel Capability Rollup for Station Group ${groupName}`}
            </Typography>
            <IconButton
              edge='start'
              color='inherit'
              onClick={() => setPopOver(false)}
              aria-label='close'
            >
              <CloseIcon />
            </IconButton>
          </Toolbar>
        </AppBar>
        {rollup ? (
          <Rollup
            groupName={groupName}
            channelName={channelName}
            defaultRollup={rollup}
            rollups={rollup.rollups ?? []}
            rollupTypeOptions={rollupTypeOptions}
            operatorTypeOptions={operatorTypeOptions}
          />
        ) : undefined}
      </Dialog>
    </div>
  );
};
