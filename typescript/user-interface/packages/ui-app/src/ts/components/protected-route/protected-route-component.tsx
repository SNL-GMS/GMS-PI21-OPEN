import { useAppDispatch, useAppSelector } from '@gms/ui-state';
import React from 'react';

import { KeyCloakService } from '~app/authentication/gms-keycloak';
import { GMS_DISABLE_KEYCLOAK_AUTH } from '~env';

import { GMSNavigate } from './gms-navigate-component';

function useDocumentTitle(title, prevailOnUnmount = false) {
  const defaultTitle = React.useRef(document.title);

  React.useEffect(() => {
    if (title) {
      document.title = title;
    }
  }, [title]);

  React.useEffect(
    () => () => {
      if (!prevailOnUnmount) {
        if (defaultTitle.current) {
          document.title = defaultTitle.current;
        }
      }
    },
    [prevailOnUnmount]
  );
}

/**
 * Routes to the login screen if the user session is not authenticated
 */
export function ProtectedRouteComponent(props: {
  children?: JSX.Element;
  redirectPath?: string;
  title?: string;
}) {
  const { children, redirectPath, title } = props;
  const dispatch = useAppDispatch();
  const authenticated = useAppSelector(
    state => state.app.userSession.authenticationStatus.authenticated
  );
  React.useLayoutEffect(() => {
    if (!GMS_DISABLE_KEYCLOAK_AUTH && !authenticated) {
      KeyCloakService.updateUserAuthenticationStatus()
        .then(getAuthenticationStatus => dispatch(getAuthenticationStatus))
        .catch(e => console.error(e));
    }
  }, [authenticated, dispatch]);
  useDocumentTitle(title);
  const toValue = React.useMemo(() => {
    return {
      pathname: GMS_DISABLE_KEYCLOAK_AUTH ? `/login/${redirectPath || ''}` : `${redirectPath || ''}`
    };
  }, [redirectPath]);
  if (!authenticated) {
    return (
      // using custom navigate due to bug https://github.com/remix-run/react-router/issues/8733
      <GMSNavigate to={toValue} />
    );
  }
  return children;
}
