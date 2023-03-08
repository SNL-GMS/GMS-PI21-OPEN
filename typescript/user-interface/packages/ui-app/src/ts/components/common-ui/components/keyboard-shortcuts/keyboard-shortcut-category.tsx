import { H5 } from '@blueprintjs/core';
import React from 'react';

interface KeyboardShortcutCategoryProps {
  children: React.ReactNode;
  catName: string;
}
/**
 * Creates a keyboard shortcut category, containing a header. Renders its children.
 */
export function KeyboardShortcutCategory(props: KeyboardShortcutCategoryProps) {
  const { catName, children } = props;

  return (
    <section className="keyboard-shortcuts__category">
      <H5 className="keyboard-shortcuts__group-title">{catName}</H5>
      {children}
    </section>
  );
}
