import React from 'react';
import { Routes, Route, useLocation } from 'react-router-dom';
import App from './App';
import { AppSettings } from './routes/AppSettings';
import { RawData } from './routes/RawData';
import { Station } from './routes/Station';

/**
 * The type of the props for the {@link AppRouter} component
 */
export interface AppRouterProps {}

/**
 * Creates the routes for the app, and handles transitions.
 * Must be wrapped in a React Routes HashRouter component.
 */
export const AppRouter: React.FC<AppRouterProps> = () => {
  const location = useLocation();
  const [displayLocation, setDisplayLocation] = React.useState(location);
  const [transitionStage, setTransitionStage] = React.useState<
    'fadeIn' | 'fadeOut'
  >('fadeIn');
  React.useEffect(() => {
    if (location !== displayLocation) {
      setTransitionStage('fadeOut');
    }
  }, [location, displayLocation]);
  const onTransitionEnd = React.useCallback(() => {
    if (transitionStage === 'fadeOut') {
      setTransitionStage('fadeIn');
      setDisplayLocation(location);
    }
  }, [location, transitionStage]);
  return (
    <Routes location={displayLocation}>
      <Route
        path='/'
        element={
          <App
            transitionStage={transitionStage}
            onTransitionEnd={onTransitionEnd}
          />
        }
      >
        <Route index element={<Station />} />
        <Route path='raw-data' element={<RawData />} />
        <Route path='app-settings' element={<AppSettings />} />
      </Route>
    </Routes>
  );
};
