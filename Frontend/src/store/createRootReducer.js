import { combineReducers } from 'redux';
import userReducer from './user/user.reducer';

const createRootReducer = () =>
  combineReducers({
    user: userReducer,
  });

export default createRootReducer;