import { Grid } from '@mui/material';
import React from 'react';
import { RollupEntry, RollupType } from '../../state/station-controls-slice';
import makeStyles from '@mui/styles/makeStyles';
import { indentedContainer, layoutStyles } from '../../styles/layout';
import { CapabilityChannelChecklist } from './CapabilityChannelChecklist';
import { CapabilityMonitorChecklist } from './CapabilityMonitorChecklist';

const useStyles = makeStyles({
  ...layoutStyles,
  container: {
    ...indentedContainer,
    paddingLeft: '2rem',
  },
});

/**
 * The type of the props for the {@link CapabilityRollupEntry} component
 */
export interface CapabilityRollupEntryProps {
  rollup: RollupEntry;
  groupName: string;
  channelName?: string;
}

/**
 * Creates an entry for a capability rollup, which is either a checklist, or nothing
 */
export const CapabilityRollupEntry: React.FC<CapabilityRollupEntryProps> = ({
  rollup,
  groupName,
  channelName,
}: CapabilityRollupEntryProps) => {
  const classes = useStyles();
  return (
    <>
      <Grid
        container
        justifyContent={'flex-start'}
        direction={'row'}
        spacing={1}
        className={classes.container}
      >
        {rollup.rollupType === RollupType.ROLLUP_OF_CHANNELS ? (
          <CapabilityChannelChecklist
            groupName={groupName}
            rollupChannels={rollup.channels ?? []}
            rollupId={rollup.id}
          />
        ) : rollup.rollupType === RollupType.ROLLUP_OF_MONITORS ? (
          <CapabilityMonitorChecklist
            groupName={groupName}
            rollupMonitorNames={rollup.monitors ?? []}
            rollupId={rollup.id}
            channelName={channelName ?? ''}
          />
        ) : undefined}
      </Grid>
    </>
  );
};
