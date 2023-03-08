// TODO fix this test suite in the testing fix stories
/* eslint-disable react/jsx-props-no-spreading */
// import { configureNewStore } from '@gms/ui-state/__tests__/test-util';
// import React from 'react';
// import { Provider } from 'react-redux';
// import { HashRouter, Route, Routes } from 'react-router-dom';

import { ProtectedRouteComponent } from '../../src/ts/components/protected-route/protected-route-component';

// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports
// const Enzyme = require('enzyme');

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

// const props: any = {
//   path: 'somePath'
// };
describe('Protected route', () => {
  it('should be defined', () => {
    expect(ProtectedRouteComponent).toBeDefined();
  });
  // const store = configureNewStore();
  // const protectedRouteRenderAuthenticated: any = Enzyme.mount(
  //   <Provider store={store}>
  //     <HashRouter>
  //       <Routes>
  //         <Route
  //           path={props.path}
  //           element={
  //             <ProtectedRouteComponent>
  //               <div />
  //             </ProtectedRouteComponent>
  //           }
  //         />
  //       </Routes>
  //     </HashRouter>
  //   </Provider>
  // );

  // it('Authenticated should match snapshot', () => {
  //   expect(protectedRouteRenderAuthenticated).toMatchSnapshot();
  //   expect(props.render).toHaveBeenCalled();
  // });

  // props.authenticated = false;

  // // eslint-disable-next-line react/jsx-props-no-spreading
  // const protectedRouteRedirect: any = Enzyme.shallow(<ProtectedRouteComponent {...props} />);

  // it('Not authenticated should match snapshot', () => {
  //   expect(protectedRouteRedirect).toMatchSnapshot();
  // });
});
