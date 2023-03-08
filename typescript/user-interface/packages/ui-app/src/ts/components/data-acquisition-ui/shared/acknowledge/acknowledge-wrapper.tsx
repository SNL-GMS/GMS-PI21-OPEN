import type { SohTypes } from '@gms/common-model';
import {
  useAcknowledgeSohStatus,
  useAppSelector,
  useGetSohConfigurationQuery
} from '@gms/ui-state';
import { UILogger } from '@gms/ui-util';
import * as React from 'react';
import { toast } from 'react-toastify';

import { isAcknowledgeEnabled } from '../table/utils';
import type { AcknowledgeWrapperProps } from './types';

const logger = UILogger.create('GMS_LOG_SOH_ACKNOWLEDGE', process.env.GMS_LOG_SOH_ACKNOWLEDGE);

/**
 * Creates a clone of the wrapped component, and injects the acknowledgeSohStatus
 * function into the child's props.
 */
export function AcknowledgeWrapper(props: React.PropsWithChildren<AcknowledgeWrapperProps>) {
  const { sohStatus, children } = props;

  // Using React Ref to make sure the sohStationStaleMs value is not captured
  // as undefined when the FunctionComponent is created
  const query = useGetSohConfigurationQuery();
  const sohStationStaleMsRef = React.useRef(undefined);
  React.useEffect(() => {
    sohStationStaleMsRef.current = query.data?.sohStationStaleMs;
  }, [query.data?.sohStationStaleMs]);

  const callAcknowledgeMutation = useAcknowledgeSohStatus();
  const userSessionState = useAppSelector(state => state.app.userSession);
  /**
   * Call the mutation function and save the new state to the backend.
   *
   * @param stationIds modified station ids
   * @param comment (optional) an optional comment for the acknowledgement
   */
  const acknowledgeStationsByName = (stationNames: string[], comment?: string) => {
    // If station entries are not stale or already acknowledged then
    // call acknowledge mutation with station names
    if (
      isAcknowledgeEnabled(
        stationNames,
        sohStatus.stationAndStationGroupSoh.stationSoh,
        sohStationStaleMsRef.current
      )
    ) {
      try {
        const ackArgs: SohTypes.AcknowledgeSohStatus = {
          stationNames,
          comment,
          userName: userSessionState.authenticationStatus.userName
        };
        // eslint-disable-next-line @typescript-eslint/no-floating-promises
        callAcknowledgeMutation(ackArgs);
      } catch (e) {
        logger.warn(`Failed to send SOH Acknowledgement.`);
      }
    } else {
      toast.warn('Cannot acknowledge due to stale SOH data');
    }
  };

  /**
   * We store the child in a variable so we can check its type with the
   * isValidElement type guard below.
   */
  const child: React.ReactNode = children;

  /**
   * Verify that the children are indeed a React Element (not just a node)
   */
  if (React.isValidElement(child)) {
    /**
     * React.cloneElement is used to inject the new props into the child. This injects the
     * acknowledgeStationsByName function as a prop into the child.
     * CloneElement should be reasonably performant.
     * See https://stackoverflow.com/questions/54922160/react-cloneelement-in-list-performance
     */
    return <>{React.cloneElement(child, { acknowledgeStationsByName })}</>;
  }
  return undefined;
}
