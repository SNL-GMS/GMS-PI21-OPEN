import { Paper } from '@mui/material';
import React from 'react';
import logo from '../logo.svg';

/**
 * The type of the props for the {@link Nav} component
 */
export interface NavProps {}

/**
 * Creates the app level navigation
 */
export const Nav: React.FC<React.PropsWithChildren<NavProps>> = (
  props: React.PropsWithChildren<NavProps>
) => {
  return (
    <Paper className='App' elevation={5}>
      <header className='App-header'>
        <img src={logo} className='App-logo' alt='logo' />
        <h1 className='App-title'>SOH Configuration Tool</h1>
        <nav className='nav--right'>{props.children}</nav>
      </header>
    </Paper>
  );
};
