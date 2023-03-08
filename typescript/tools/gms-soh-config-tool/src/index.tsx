import {
  createTheme,
  CssBaseline,
  StyledEngineProvider,
  ThemeProvider,
} from '@mui/material';
import { createRoot } from 'react-dom/client';
import { Provider } from 'react-redux';
import { HashRouter } from 'react-router-dom';
import { AppRouter } from './AppRouter';
import { store } from './state/store';

/**
 * Customize form so each control has more space
 */
const theme = createTheme({
  components: {
    MuiFormControl: {
      styleOverrides: {
        root: {
          margin: '0.8em 0',
        },
      },
    },
    MuiFilledInput: {
      styleOverrides: {
        input: {
          paddingTop: '8px',
        },
      },
    },
  },
});

const container = document.getElementById('root');
if (container) {
  const root = createRoot(container);
  root.render(
    <StyledEngineProvider injectFirst>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <Provider store={store}>
          <HashRouter>
            <AppRouter />
          </HashRouter>
        </Provider>
      </ThemeProvider>
    </StyledEngineProvider>
  );
}
