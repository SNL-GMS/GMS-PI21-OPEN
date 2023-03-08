import { Intent, NonIdealState, Spinner } from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import { Displays } from '@gms/common-model';
import { IanDisplays } from '@gms/common-model/lib/displays/types';
import { IS_MODE_IAN } from '@gms/common-util';
import { getStore, useUiTheme, withReduxProvider } from '@gms/ui-state';
import * as React from 'react';
import { Provider } from 'react-redux';
import { HashRouter, Route, Routes } from 'react-router-dom';

import { AnalystUiComponents } from '~analyst-ui/';
import {
  IANMap,
  SignalDetectionsComponent,
  StationPropertiesComponent
} from '~analyst-ui/components';
import { FilterManager } from '~analyst-ui/components/filters/filter-manager';
import { InteractionConsumer } from '~analyst-ui/interactions/interaction-consumer';
import { InteractionProvider } from '~analyst-ui/interactions/interaction-provider';
import { authenticator } from '~app/authentication';
import { EffectiveNowTimeInitializer } from '~app/initializers/effective-time-now-initializer';
import { UIThemeWrapper } from '~app/initializers/ui-theme-wrapper';
import { ServiceWorkerController } from '~app/service-worker-controller';
import { ThemedToastContainer } from '~app/themed-toast-container';
import { CommonCommandRegistrar } from '~components/common-ui/commands';
import { CommandPalette } from '~components/common-ui/components/command-palette';
import { LoadingScreen } from '~components/loading-screen';
import { LoginScreen } from '~components/login-screen';
import { ProtectedRoute } from '~components/protected-route';
import { GoldenLayoutContext, Workspace } from '~components/workspace';
import { GMS_DISABLE_KEYCLOAK_AUTH } from '~env';

import { createPopoutComponent } from '../create-popout-component';
import { glContextData } from './golden-layout-config';

const store = getStore();

/**
 * Wraps the component route (not for SOH).
 * Provides the required context providers to the component.
 *
 * @param Component the component route
 * @param props the props passed down from the route to the component
 * @param suppressPopinIcon true to force suppress the golden-layout popin icon
 */
function wrap(Component: any, props: any, suppressPopinIcon = false) {
  function WrappedComponent(p) {
    const [uiTheme] = useUiTheme();
    return (
      <ServiceWorkerController>
        <>
          <UIThemeWrapper key={uiTheme.name}>
            <CommandPalette>
              {/* eslint-disable-next-line react/jsx-props-no-spreading */}
              <Component {...p} />
              <InteractionProvider>
                <InteractionConsumer />
              </InteractionProvider>
              <CommonCommandRegistrar />
            </CommandPalette>
            <ThemedToastContainer />
          </UIThemeWrapper>
          <EffectiveNowTimeInitializer />
          <FilterManager />
        </>
      </ServiceWorkerController>
    );
  }

  return createPopoutComponent(withReduxProvider(WrappedComponent), props, suppressPopinIcon);
}

const WrappedWorkflow: React.FC<unknown> = props => wrap(AnalystUiComponents.Workflow, props);
const WrappedEvents: React.FC<unknown> = props => wrap(AnalystUiComponents.Events, props);
const WrappedFilters: React.FC<unknown> = props => wrap(AnalystUiComponents.Filters, props);
const WrappedWaveformDisplay: React.FC<unknown> = props =>
  wrap(AnalystUiComponents.WaveformDisplay, props);
const WrappedMap: React.FC<unknown> = props => wrap(IANMap, props);
const WrappedSignalDetections: React.FC<unknown> = props => wrap(SignalDetectionsComponent, props);
const WrappedStationProperties: React.FC<unknown> = props =>
  wrap(StationPropertiesComponent, props);
const WrappedWorkspace: React.FC<unknown> = props => wrap(Workspace, props, true);

export function App(): React.ReactElement {
  // TODO: either re-route or make this a <404 component>
  const redirectRoute = GMS_DISABLE_KEYCLOAK_AUTH ? (
    <>
      <Route path="/login/:redirectUrl" element={<LoginScreen authenticator={authenticator} />} />
      <Route path="/login" element={<LoginScreen authenticator={authenticator} />} />
    </>
  ) : (
    <Route path="/:redirectUrl" />
  );

  return !IS_MODE_IAN ? (
    <NonIdealState
      icon={IconNames.ERROR}
      action={<Spinner intent={Intent.DANGER} />}
      title="Invalid settings"
      description="Not configured for IAN mode - Please check settings"
    />
  ) : (
    <Provider store={store}>
      <HashRouter>
        {
          // ! CAUTION: when changing the route paths
          // The route paths must match the `golden-layout` component name for popout windows
          // For example, the component name `my-route` must have the route path of `my-route`
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
          {redirectRoute}
          <Route
            path="/loading"
            element={
              <ProtectedRoute redirectPath="loading" title="GMS: Loading...">
                <LoadingScreen />
              </ProtectedRoute>
            }
          />
          {
            // Individual ProtectedRoutes
          }
          <Route
            path={`/${IanDisplays.WORKFLOW}`}
            element={
              <ProtectedRoute
                redirectPath={IanDisplays.WORKFLOW}
                title={Displays.toDisplayTitle(Displays.IanDisplays.WORKFLOW, 'GMS: ')}
              >
                <WrappedWorkflow />
              </ProtectedRoute>
            }
          />
          <Route
            path={`/${IanDisplays.EVENTS}`}
            element={
              <ProtectedRoute
                redirectPath={IanDisplays.EVENTS}
                title={Displays.toDisplayTitle(Displays.IanDisplays.EVENTS, 'GMS: ')}
              >
                <WrappedEvents />
              </ProtectedRoute>
            }
          />
          <Route
            path={`/${IanDisplays.FILTERS}`}
            element={
              <ProtectedRoute
                redirectPath={IanDisplays.FILTERS}
                title={Displays.toDisplayTitle(Displays.IanDisplays.FILTERS, 'GMS: ')}
              >
                <WrappedFilters />
              </ProtectedRoute>
            }
          />
          <Route
            path={`/${IanDisplays.WAVEFORM}`}
            element={
              <ProtectedRoute
                redirectPath={IanDisplays.WAVEFORM}
                title={Displays.toDisplayTitle(Displays.IanDisplays.WAVEFORM, 'GMS: ')}
              >
                <WrappedWaveformDisplay />
              </ProtectedRoute>
            }
          />
          <Route
            path={`/${IanDisplays.MAP}`}
            element={
              <ProtectedRoute
                redirectPath={IanDisplays.MAP}
                title={Displays.toDisplayTitle(Displays.IanDisplays.MAP, 'GMS: ')}
              >
                <WrappedMap />
              </ProtectedRoute>
            }
          />
          <Route
            path={`/${IanDisplays.SIGNAL_DETECTIONS}`}
            element={
              <ProtectedRoute
                redirectPath={IanDisplays.SIGNAL_DETECTIONS}
                title={Displays.toDisplayTitle(Displays.IanDisplays.SIGNAL_DETECTIONS, 'GMS: ')}
              >
                <WrappedSignalDetections />
              </ProtectedRoute>
            }
          />
          <Route
            path={`/${IanDisplays.STATION_PROPERTIES}`}
            element={
              <ProtectedRoute
                redirectPath={IanDisplays.STATION_PROPERTIES}
                title={Displays.toDisplayTitle(Displays.IanDisplays.STATION_PROPERTIES, 'GMS: ')}
              >
                <WrappedStationProperties />
              </ProtectedRoute>
            }
          />
          {
            // Workspace
          }
          <Route
            path="*"
            element={
              <ProtectedRoute title="GMS: App Workspace">
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
