/* eslint-disable react/prop-types */
import { Classes } from '@blueprintjs/core';
import type { SystemMessageTypes } from '@gms/common-model';
import { DropDown } from '@gms/ui-core-components';
import classNames from 'classnames';
import get from 'lodash/get';
import uniq from 'lodash/uniq';
import React from 'react';

import type { SelectedOptions } from './types';
import { ALL_CATEGORIES, ALL_SEVERITIES, ALL_SUBCATEGORIES, FILTER_TYPE } from './types';

interface SoundConfigurationToolbarProps {
  systemMessageDefinitions: SystemMessageTypes.SystemMessageDefinition[];
  selectedOptions: SelectedOptions;
  onChanged(type: FILTER_TYPE, value): void;
}

// eslint-disable-next-line react/function-component-definition
export const SoundConfigurationToolbar: React.FunctionComponent<SoundConfigurationToolbarProps> = ({
  systemMessageDefinitions,
  selectedOptions,
  onChanged
}) => {
  const { selectedSeverity, selectedCategory, selectedSubcategory } = selectedOptions;
  // Filter unique states to create dropdown items from an array of systemMessageDefinitions
  const genList = (messageProp: string, defaultState: string) =>
    Object.assign(
      uniq(
        systemMessageDefinitions?.map(systemMessageDefinition =>
          get(systemMessageDefinition, messageProp)
        )
      ).reduce((prev, curr) => {
        // eslint-disable-next-line no-param-reassign
        prev[curr] = curr;
        return prev;
      }, {}),
      { default: defaultState }
    );

  const severityList = genList('systemMessageSeverity', ALL_SEVERITIES);
  const categoryList = genList('systemMessageCategory', ALL_CATEGORIES);
  const subcategoryList = genList('systemMessageSubCategory', ALL_SUBCATEGORIES);

  return (
    <div className={classNames(Classes.HTML_SELECT, 'sound-configuration-toolbar')}>
      <div className="sound-configuration-toolbar__dropdown">
        <DropDown
          dropDownItems={severityList}
          value={selectedSeverity}
          title="Filter by severity"
          onMaybeValue={(value: any) => {
            onChanged(FILTER_TYPE.SEVERITY, value);
          }}
        />
      </div>

      <div className="sound-configuration-toolbar__dropdown">
        <DropDown
          dropDownItems={categoryList}
          value={selectedCategory}
          title="Filter by category"
          onMaybeValue={(value: any) => {
            onChanged(FILTER_TYPE.CATEGORY, value);
          }}
        />
      </div>

      <div className="sound-configuration-toolbar__dropdown">
        <DropDown
          dropDownItems={subcategoryList}
          value={selectedSubcategory}
          title="Filter by subcategory"
          onMaybeValue={(value: any) => {
            onChanged(FILTER_TYPE.SUBCATEGORY, value);
          }}
        />
      </div>
    </div>
  );
};
