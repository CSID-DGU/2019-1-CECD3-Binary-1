import { combineReducers } from 'redux';
import userReducer from './user/user.reducer';
import urlReducer from './url/url.reducer';

const createRootReducer = () =>
  combineReducers({
    user: userReducer,
    url: urlReducer,
  });

export default createRootReducer;