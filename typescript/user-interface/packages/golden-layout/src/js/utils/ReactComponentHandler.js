/**
 * A specialised GoldenLayout component that binds GoldenLayout container
 * lifecycle events to react components
 *
 * @constructor
 * @param container
 * @param state state is not required for react components
 */
lm.utils.ReactComponentHandler = function (container, state) {
  // Keep reference to the root for unmounting.
  this._root = null;
  this._reactComponent = null;
  this._originalComponentWillUpdate = null;
  this._container = container;
  this._initialState = state;
  this._reactClass = this._getReactClass();
  this._container.on('open', this._render, this);
  this._container.on('destroy', this._destroy, this);
};

lm.utils.copy(lm.utils.ReactComponentHandler.prototype, {
  /**
   * Creates the react class and component and hydrates it with
   * the initial state - if one is present
   *
   * By default, react's getInitialState will be used
   *
   * @private
   * @returns
   */
  _render() {
    // support React 18, which requires createRoot rather than ReactDOM.render
    if (createRoot) {
      this._root = createRoot(this._container.getElement()[0]);
      this._root.render(this._getReactComponent());
    } else if (ReactDOM) {
      this._reactComponent = ReactDOM.render(
        this._getReactComponent(),
        this._container.getElement()[0]
      );
    } else {
      throw new Error(
        'No version of createRoot (React 18) or ReactDOM (older versions of React) found. Make sure to add it to the global namespace.'
      );
    }
    // support react 16 & 18: if we haven't had a component returned yet, create one using a portal.
    if (!this._reactComponent) {
      this._reactComponent = ReactDOM.createPortal(
        this._getReactComponent(),
        this._container.getElement()[0]
      );
    }
    this._originalComponentWillUpdate = this._reactComponent.componentWillUpdate || function () {};
    this._reactComponent.componentWillUpdate = this._onUpdate.bind(this);
    if (this._container.getState()) {
      this._reactComponent.setState(this._container.getState());
    }
  },

  /**
   * Removes the component from the DOM and thus invokes React's unmount lifecycle
   *
   * @private
   * @returns
   */
  _destroy() {
    if (this._root != null) {
      // Unmount the root rather than use outdated `ReactDOM.unmountComponentAtNode`
      this._root.unmount();
    } else {
      // If not in React 18, use the old way.
      ReactDOM.unmountComponentAtNode(this._container.getElement()[0]);
    }
    this._container.off('open', this._render, this);
    this._container.off('destroy', this._destroy, this);
  },

  /**
   * Hooks into React's state management and applies the componentstate
   * to GoldenLayout
   *
   * @private
   * @returns
   */
  _onUpdate(nextProps, nextState) {
    this._container.setState(nextState);
    this._originalComponentWillUpdate.call(this._reactComponent, nextProps, nextState);
  },

  /**
   * Retrieves the react class from GoldenLayout's registry
   *
   * @private
   * @returns
   */
  _getReactClass() {
    const componentName = this._container._config.component;
    let reactClass;

    if (!componentName) {
      throw new Error('No react component name. type: react-component needs a field `component`');
    }

    reactClass = this._container.layoutManager.getComponent(componentName);

    if (!reactClass) {
      throw new Error(
        `React component "${componentName}" not found. ` +
          `Please register all components with GoldenLayout using \`registerComponent(name, component)\``
      );
    }

    return reactClass;
  },

  /**
   * Copies and extends the properties array and returns the React element
   *
   * @private
   * @returns
   */
  _getReactComponent() {
    const defaultProps = {
      glEventHub: this._container.layoutManager.eventHub,
      glContainer: this._container
    };
    const props = $.extend(defaultProps, this._container._config.props);
    return React.createElement(this._reactClass, props);
  }
});
