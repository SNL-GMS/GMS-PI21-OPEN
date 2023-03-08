/* eslint-disable react/destructuring-assignment */
/* eslint-disable react/prop-types */
import defer from 'lodash/defer';
import * as React from 'react';

import { CommandPaletteContext } from '~components/common-ui/components/command-palette/command-palette-context';
import { CommandScope } from '~components/common-ui/components/command-palette/types';

import { AcknowledgeOverlay } from '../shared/acknowledge';
import { createAcknowledgementCommands } from './acknowledgement-commands';
import { createSelectionCommands } from './selection-commands';
import type { CommandRegistrarProps } from './types';

/**
 * Registers data acquisition commands:
 * Acknowledgement commands, selection commands.
 * Creates acknowledge overlay that can be shown when a user executes the acknowledge command.
 */
export const CommandRegistrarComponent: React.FunctionComponent<React.PropsWithChildren<
  CommandRegistrarProps
  // eslint-disable-next-line react/function-component-definition
>> = props => {
  // eslint-disable-next-line @typescript-eslint/unbound-method
  const { registerCommands } = React.useContext(CommandPaletteContext);
  const [ackOverlayIsOpen, setAckOverlayIsOpen] = React.useState(false);
  const [stationsToAck, setStationsToAck] = React.useState<string[]>([]);

  const selectionCommands = createSelectionCommands(
    props.sohStatus.stationAndStationGroupSoh.stationSoh,
    props.selectedStationIds,
    s => props.setSelectedStationIds(s)
  );

  const acknowledgeCommands = createAcknowledgementCommands(
    props.sohStatus,
    props.selectedStationIds,
    (s: string[]) => {
      defer(() => {
        setAckOverlayIsOpen(true);
        setStationsToAck(s);
      });
    }
  );

  /**
   * Used to see if the commands have changed, for useEffect hook.
   */
  const commandSignature = [
    selectionCommands.map(c => c.displayText).join(),
    acknowledgeCommands.map(c => c.displayText).join()
  ];
  React.useEffect(() => {
    registerCommands([...selectionCommands, ...acknowledgeCommands], CommandScope.SOH);
    // !FIX ESLINT Validate and check REACT HOOK dependencies
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, commandSignature);

  return (
    <AcknowledgeOverlay
      acknowledgeStationsByName={(stationNames: string[], comment?: string) =>
        props.acknowledgeStationsByName(stationNames, comment)
      }
      isOpen={ackOverlayIsOpen}
      stationNames={stationsToAck}
      onClose={() => {
        setAckOverlayIsOpen(false);
        setStationsToAck([]);
      }}
      requiresModificationForSubmit={false}
    />
  );
};
