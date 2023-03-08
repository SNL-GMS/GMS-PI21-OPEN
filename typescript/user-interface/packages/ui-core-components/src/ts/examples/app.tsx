import '../../scss/ui-core-components.scss';
import './style.scss';

import { Button, ButtonGroup, Classes, Colors } from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import React from 'react';
import ReactDOM from 'react-dom';
import { HashRouter, Link, Route, Routes } from 'react-router-dom';

import { DropDownExample } from './drop-down-example';
import { FilterableOptionListExample } from './filterable-option-list-example';
import { FormNoInputExample } from './form-no-input-example';
import { FormSubmittableExample } from './form-submittable-example';
import { Home } from './home';
import { IntervalPickerExample } from './interval-picker-example';
import { TableExample } from './table-example';
import { TimePickerExample } from './time-picker-example';
import { ToolbarExample } from './toolbar-example';

(window as any).React = React;
(window as any).ReactDOM = ReactDOM;

export function App(): JSX.Element {
  return (
    <div id="app-content">
      <HashRouter>
        <div
          className={Classes.DARK}
          style={{
            height: '100%',
            width: '100%',
            padding: '0.5rem',
            color: Colors.GRAY4
          }}
        >
          <ButtonGroup minimal>
            <Button icon={IconNames.HOME}>
              <Link to="/">Home</Link>
            </Button>
          </ButtonGroup>

          <hr />
          <Routes>
            <Route path="/Table" element={<TableExample />} />
            <Route path="/FormSubmittable" element={<FormSubmittableExample />} />
            <Route path="/FormNoInput" element={<FormNoInputExample />} />
            <Route path="/DropDownExample" element={<DropDownExample />} />
            <Route path="/IntervalPickerExample" element={<IntervalPickerExample />} />
            <Route path="/TimePickerExample" element={<TimePickerExample />} />
            <Route path="/ToolbarExample" element={<ToolbarExample />} />
            <Route path="/FilterableOptionListExample" element={<FilterableOptionListExample />} />
            <Route path="*" element={<Home />} />
          </Routes>
        </div>
      </HashRouter>
    </div>
  );
}
