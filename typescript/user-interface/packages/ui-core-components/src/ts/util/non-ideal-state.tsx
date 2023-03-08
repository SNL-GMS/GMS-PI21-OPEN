import type { IconName } from '@blueprintjs/core';
import { Intent, NonIdealState, Spinner } from '@blueprintjs/core';
import React from 'react';

/**
 * Creates a NonIdealState blueprint JSX with the option of passing a title and description
 *
 * @param title optional description of the title that will be displayed
 * @param description optional description that will be displayed
 * @param icon optional the icon state that will be displayed
 */
export function nonIdealStateWithNoSpinner(
  title?: string,
  description?: string,
  icon?: IconName
): any {
  return <NonIdealState icon={icon} title={title || null} description={description || null} />;
}

/**
 * Creates a NonIdealState blueprint JSX with a spinner action
 * with the option of passing a title and description
 *
 * @param title optional description of the title that will be displayed
 * @param description optional description that will be displayed
 * @param icon optional the icon state that will be displayed
 * @param intent optional the intent state that will be displayed
 */
export function nonIdealStateWithSpinner(
  title?: string,
  description?: string,
  icon?: IconName,
  intent: Intent = Intent.PRIMARY
): JSX.Element {
  return (
    <NonIdealState
      action={<Spinner intent={intent} />}
      icon={icon}
      title={title || null}
      description={description || null}
    />
  );
}
