import React from 'react';
import ReactDOM from 'react-dom';
import createHistory from 'history/createBrowserHistory';
import { PersistGate } from 'redux-persist/integration/react';
import { Route, Router } from 'react-router-dom';
import { Provider } from 'react-redux';
import App from './App';
import configureStore from './store/configureStore';

const initialState = {};
const { store, persistor } = configureStore(initialState);
const history = createHistory();

ReactDOM.render(
  <Provider store={store}>
    <PersistGate loading={null} persistor={persistor}>
      <Router history={history}>
        <Route component={App} />
      </Router>
    </PersistGate>
  </Provider>,
  document.getElementById('root')
);

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA