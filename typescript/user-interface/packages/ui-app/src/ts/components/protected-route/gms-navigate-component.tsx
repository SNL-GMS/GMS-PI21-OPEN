import { useEffect, useRef } from 'react';
import type { NavigateProps } from 'react-router-dom';
import { useNavigate } from 'react-router-dom';

/**
 * The prop `to` inside navProps needs to be referentially stable for this to work correctly without re-renders.
 * the prop `to` is the location you want to redirect to
 * creating custom navigate due to bug {@link https://github.com/remix-run/react-router/issues/8733}
 *
 * @param navProps
 * @returns null
 */
export function GMSNavigate(navProps: NavigateProps) {
  const { to } = navProps;
  const navigate = useNavigate();
  const navigateRef = useRef(navigate);

  useEffect(() => {
    navigateRef.current = navigate;
  }, [navigate]);

  useEffect(() => {
    navigateRef.current(to);
  }, [to]);

  return null;
}
