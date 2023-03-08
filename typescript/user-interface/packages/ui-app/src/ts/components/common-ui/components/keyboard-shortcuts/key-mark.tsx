import React from 'react';

/**
 * Creates a visual representation of a key, with the key name or special character if appropriate.
 */
export function KeyMark(props) {
  const { children } = props;
  let theMark = children;
  if (typeof children === 'string') {
    theMark = children
      .replace(/command|cmd/, '⌘')
      .replace(/option/, '⌥')
      .replace(/control/, 'ctrl');
  }
  return <mark className="key-mark">{theMark}</mark>;
}
