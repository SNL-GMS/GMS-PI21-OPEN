import { Alert, H2, Intent } from '@blueprintjs/core';
import { VERSION_INFO } from '@gms/common-util';
import classNames from 'classnames';
import React from 'react';

import { VersionInfo } from '~common-ui/components/version-info/version-info';

/**
 * The type of the props for the {@link ServiceWorkerAlert} component
 */
export interface ServiceWorkerAlertProps {
  /**
   * the state of whether the alert should be shown.
   */
  isAlertOpen: boolean;
  /**
   * A ref containing a callback function which will trigger the service worker to take control of the page,
   * triggering a refresh.
   */
  skipWaitingFunctionRef: React.MutableRefObject<() => void>;
  /**
   * A function to toggle the alert to pop up and prompt the user.
   */
  toggleAlert: () => void;
}

/**
 * Creates an alert requesting that the user refresh the page because a new version of the app has loaded.
 * Shows a spinner if we just sent the service worker a signal to take control, which will refresh the page.
 */
export function ServiceWorkerAlert({
  isAlertOpen,
  skipWaitingFunctionRef,
  toggleAlert
}: ServiceWorkerAlertProps) {
  const [isRefreshing, setIsRefreshing] = React.useState(false);

  const handleConfirm = React.useCallback(() => {
    if (skipWaitingFunctionRef.current) {
      setIsRefreshing(true);
      skipWaitingFunctionRef.current();
    } else {
      throw new Error('Cannot refresh page. No skipWaiting function was provided');
    }
  }, [skipWaitingFunctionRef]);

  return (
    <Alert
      intent={isRefreshing ? Intent.SUCCESS : Intent.PRIMARY}
      className={classNames('sw-alert', { 'sw-alert__refreshing': isRefreshing })}
      cancelButtonText="Cancel"
      onCancel={toggleAlert}
      confirmButtonText="Update"
      icon="updated"
      isOpen={isAlertOpen}
      onConfirm={handleConfirm}
      loading={isRefreshing}
    >
      <div className="sw-alert__info">
        {isRefreshing ? (
          <>
            <H2>Updating</H2>
            <VersionInfo />
            <p>
              Loading version <span className="monospace">{VERSION_INFO.commitSHA}</span>
            </p>
            <p>Refreshing open tabs</p>
          </>
        ) : (
          <>
            <H2>Update Needed</H2>
            <VersionInfo />
            <p>This will refresh all open tabs</p>
            <p>Any unsaved changes will be lost</p>
          </>
        )}
      </div>
    </Alert>
  );
}
