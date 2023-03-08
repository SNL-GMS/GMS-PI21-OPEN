import { Intent, NonIdealState, Spinner } from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import { IS_MODE_SOH } from '@gms/common-util';
import {
  getStore,
  SohStatusSubscription,
  SystemMessageSubscription,
  withReduxProvider
} from '@gms/ui-state';
import * as React from 'react';
import { Provider } from 'react-redux';
import { HashRouter, Route, Routes } from 'react-router-dom';

import { authenticator } from '~app/authentication';
import { UIThemeWrapper } from '~app/initializers/ui-theme-wrapper';
import { ThemedToastContainer } from '~app/themed-toast-container';
import { SystemMessageAlertPlayer } from '~common-ui/components/system-message/audio';
import { CommonUIComponents } from '~components/common-ui';
import { CommonCommandRegistrar } from '~components/common-ui/commands';
import { CommandPalette } from '~components/common-ui/components/command-palette';
import { SohCommandRegistrar } from '~components/data-acquisition-ui/commands';
import { SohEnvironmentHistory } from '~components/data-acquisition-ui/components/environment-history';
import { SohLagHistory } from '~components/data-acquisition-ui/components/soh-lag-history';
import { SohMissingHistory } from '~components/data-acquisition-ui/components/soh-missing-history';
import { SohTimelinessHistory } from '~components/data-acquisition-ui/components/soh-timeliness-history';
import { StationStatistics } from '~components/data-acquisition-ui/components/station-statistics';
import { InteractionWrapper } from '~components/data-acquisition-ui/interactions/interaction-wrapper';
import { LoadingScreen } from '~components/loading-screen';
import { LoginScreen } from '~components/login-screen';
import { ProtectedRoute } from '~components/protected-route';
import { GoldenLayoutContext, Workspace } from '~components/workspace';
import { SohLag, SohMissing, SohTimeliness } from '~data-acquisition-ui/components/soh-bar-chart';
import { SohEnvironment } from '~data-acquisition-ui/components/soh-environment';
import { SohMap } from '~data-acquisition-ui/components/soh-map';
import { SohOverview } from '~data-acquisition-ui/components/soh-overview';

import { createPopoutComponent } from '../create-popout-component';
import { glContextData } from './golden-layout-config';

/**
 * Wraps the component route (not for SOH).
 * Provides the required context providers to the component.
 *
 * @param Component the component route
 * @param props the props passed down from the route to the component
 * @param suppressPopinIcon true to force suppress the golden-layout popin icon
 */
function wrap(Component: any, props: any, suppressPopinIcon = false) {
  const InteractionComp = InteractionWrapper(Component);

  function WrappedComponent(p) {
    return (
      <CommandPalette>
        {/* eslint-disable-next-line react/jsx-props-no-spreading */}
        <InteractionComp {...p} />
        <CommonCommandRegistrar />
        <ThemedToastContainer />
        <SystemMessageAlertPlayer />
      </CommandPalette>
    );
  }

  return createPopoutComponent(
    withReduxProvider(
      SystemMessageSubscription.wrapSystemMessageSubscription(WrappedComponent, props)
    ),
    props,
    suppressPopinIcon
  );
}

/**
 * Wraps the component route for SOH.
 * Provides the required context providers to the component.
 *
 * @param Component the component route
 * @param props the props passed down from the route to the component
 * @param suppressPopinIcon true to force suppress the golden-layout popin icon
 */
function wrapSoh(Component: any, props: any, suppressPopinIcon = false) {
  const InteractionComp = InteractionWrapper(Component);

  function WrappedComponent(p) {
    return (
      <UIThemeWrapper>
        <CommandPalette>
          {/* eslint-disable-next-line react/jsx-props-no-spreading */}
          <InteractionComp {...p} />
          <SohCommandRegistrar />
          <CommonCommandRegistrar />
        </CommandPalette>
        <ThemedToastContainer />
        <SystemMessageAlertPlayer />
      </UIThemeWrapper>
    );
  }
  return createPopoutComponent(
    withReduxProvider(
      SystemMessageSubscription.wrapSystemMessageSubscription(
        SohStatusSubscription.wrapSohStatusSubscription(WrappedComponent, props),
        props
      )
    ),
    props,
    suppressPopinIcon
  );
}

const WrappedSystemMessage: React.FC = props => wrap(CommonUIComponents.SystemMessage, props);
const WrappedOverview: React.FC = props => wrapSoh(SohOverview, props);
const WrappedStatistics: React.FC = props => wrapSoh(StationStatistics, props);
const WrappedEnvironment: React.FC = props => wrapSoh(SohEnvironment, props);
const WrappedMissing: React.FC = props => wrapSoh(SohMissing, props);
const WrappedLag: React.FC = props => wrapSoh(SohLag, props);
const WrappedEnvironmentTrends: React.FC = props => wrapSoh(SohEnvironmentHistory, props);
const WrappedLagTrends: React.FC = props => wrapSoh(SohLagHistory, props);
const WrappedMissingTrends: React.FC = props => wrapSoh(SohMissingHistory, props);
const WrappedTimelinessTrends: React.FC = props => wrapSoh(SohTimelinessHistory, props);
const WrappedTimeliness: React.FC = props => wrapSoh(SohTimeliness, props);
const WrappedMap: React.FC = props => wrapSoh(SohMap, props);
const WrappedWorkspace: React.FC = props => wrapSoh(Workspace, props, true);

export function App(): React.ReactElement {
  return !IS_MODE_SOH ? (
    <NonIdealState
      icon={IconNames.ERROR}
      action={<Spinner intent={Intent.DANGER} />}
      title="Invalid settings"
      description="Not configured for SOH mode - Please check settings"
    />
  ) : (
    <Provider store={getStore()}>
      <HashRouter>
        {
          // ! CAUTION: when changing the route paths
          // The route paths must match the `golden-layout` component name for popout windows
          // For example, the component name `signal-detections` must have the route path of `signal-detections`
        }
        {
          // For performance use `render` which accepts a functional component
          // that won't get unnecessarily remounted like with component.
        }
        <Routes>
          {
            // Authentication
            // React router dropped support for optional params
            // so we need separate routes for login and login with a redirect
          }
          <Route
            path="/login/:redirectUrl"
            element={<LoginScreen authenticator={authenticator} />}
          />
          <Route path="/login" element={<LoginScreen authenticator={authenticator} />} />
          <Route
            path="/loading"
            element={
              <ProtectedRoute>
                <LoadingScreen />
              </ProtectedRoute>
            }
          />
          {
            // Common UI
          }
          <Route
            path="/system-messages"
            element={
              <ProtectedRoute>
                <WrappedSystemMessage />
              </ProtectedRoute>
            }
          />
          {
            // Data Acquisition
          }
          <Route
            path="/soh-overview"
            element={
              <ProtectedRoute>
                <WrappedOverview />
              </ProtectedRoute>
            }
          />
          <Route
            path="/station-statistics"
            element={
              <ProtectedRoute>
                <WrappedStatistics />
              </ProtectedRoute>
            }
          />
          <Route
            path="/soh-environment"
            element={
              <ProtectedRoute>
                <WrappedEnvironment />
              </ProtectedRoute>
            }
          />
          <Route
            path="/soh-missing"
            element={
              <ProtectedRoute>
                <WrappedMissing />
              </ProtectedRoute>
            }
          />
          <Route
            path="/soh-lag"
            element={
              <ProtectedRoute>
                <WrappedLag />
              </ProtectedRoute>
            }
          />
          <Route
            path="/soh-environment-trends"
            element={
              <ProtectedRoute>
                <WrappedEnvironmentTrends />
              </ProtectedRoute>
            }
          />
          <Route
            path="/soh-lag-trends"
            element={
              <ProtectedRoute>
                <WrappedLagTrends />
              </ProtectedRoute>
            }
          />
          <Route
            path="/soh-missing-trends"
            element={
              <ProtectedRoute>
                <WrappedMissingTrends />
              </ProtectedRoute>
            }
          />
          <Route
            path="/soh-timeliness-trends"
            element={
              <ProtectedRoute>
                <WrappedTimelinessTrends />
              </ProtectedRoute>
            }
          />
          <Route
            path="/soh-timeliness"
            element={
              <ProtectedRoute>
                <WrappedTimeliness />
              </ProtectedRoute>
            }
          />
          <Route
            path="/soh-map"
            element={
              <ProtectedRoute>
                <WrappedMap />
              </ProtectedRoute>
            }
          />
          {
            // Workspace
          }
          <Route
            path="*"
            element={
              <ProtectedRoute>
                <GoldenLayoutContext.Provider value={glContextData()}>
                  <WrappedWorkspace />
                </GoldenLayoutContext.Provider>
              </ProtectedRoute>
            }
          />
        </Routes>
      </HashRouter>
    </Provider>
  );
}
