import { createStore, applyMiddleware, compose } from 'redux';
import createSagaMiddleware from 'redux-saga';

import storage from 'redux-persist/lib/storage';
import { persistStore, persistReducer } from 'redux-persist';

import createRootSaga from './createRootSaga';
import createRootReducer from './createRootReducer';

const sagaMiddleware = createSagaMiddleware();

const persistConfig = {
  key: 'root',
  storage,
  whitelist: ['user'],
};

export default initialState => {
  const persistedReducer = persistReducer(
    persistConfig,
    createRootReducer()
  );

  const store = createStore(
    persistedReducer,
    initialState,
    compose(applyMiddleware(sagaMiddleware))
  );
  const persistor = persistStore(store);

  sagaMiddleware.run(createRootSaga);

  return {
    store,
    persistor,
  };
};