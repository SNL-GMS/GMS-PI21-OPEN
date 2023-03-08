import { useUiTheme } from '@gms/ui-state';
import React from 'react';
import { Slide, ToastContainer } from 'react-toastify';

/**
 * Creates a ToastContainer with the default values, and gives it the GMS Theme
 */
// eslint-disable-next-line react/function-component-definition
export const ThemedToastContainer: React.FC<Record<string, never>> = () => {
  const [uiTheme] = useUiTheme();
  return (
    <ToastContainer
      transition={Slide}
      autoClose={4000}
      position="bottom-right"
      theme={uiTheme.isDarkMode ? 'dark' : 'light'}
    />
  );
};
