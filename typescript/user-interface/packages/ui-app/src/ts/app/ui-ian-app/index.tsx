// combine scss of all components
// !THE ORDER OF IMPORTS MATTERS FOR OVERRIDING STYLES
/* eslint-disable import/order */
import '@blueprintjs/icons/lib/css/blueprint-icons.css';
// eslint-disable-next-line no-restricted-imports
import '@gms/weavess/src/scss/weavess.scss';
// core components styles need to override blueprint and weavess
// eslint-disable-next-line no-restricted-imports
import '@gms/ui-core-components/src/scss/ui-core-components.scss';
// eslint-disable-next-line import/no-unresolved
import 'cesium/Widgets/widgets.css';
// TODO IAN import app specific styles - do not import soh styles
import '../../../css/ui-app.scss';

import { isDarkMode, replaceFavIcon } from '@gms/ui-util';
import { enableAllPlugins } from 'immer';
import * as JQuery from 'jquery';
import React from 'react';
import ReactDom from 'react-dom';
import { createRoot } from 'react-dom/client';

import { KeyCloakService } from '~app/authentication/gms-keycloak';
import { GMS_DISABLE_KEYCLOAK_AUTH } from '~env';

import { checkEnvConfiguration } from '../check-env-configuration';
import { checkUserAgent } from '../check-user-agent';
import { configureElectron } from '../configure-electron';
import { configureReactPerformanceDevTool } from '../configure-react-performance-dev-tool';
import { App } from './app';
/* eslint-enable import/order */

// required for golden-layout
(window as any).React = React;
(window as any).ReactDOM = ReactDom;
(window as any).createRoot = createRoot;
(window as any).$ = JQuery;

window.onload = () => {
  checkEnvConfiguration();
  checkUserAgent();
  configureReactPerformanceDevTool();
  enableAllPlugins();

  // if the user is in dark mode, we replace the favicon with a lighter icon so it is visible
  if (isDarkMode()) {
    // eslint-disable-next-line global-require, @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports, import/no-unresolved
    const logo = require('../../../../resources/images/gms-logo-favicon-dark.png');
    replaceFavIcon(logo);
  }

  const root = createRoot(document.getElementById('app'));
  const renderApp = () => root.render(<App />);
  if (GMS_DISABLE_KEYCLOAK_AUTH) {
    renderApp();
  } else KeyCloakService.callLogin(renderApp);
};

configureElectron();
