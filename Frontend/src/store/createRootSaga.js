import { all } from 'redux-saga/effects';
import { UserSagas } from './user/user.saga';

export default function* createRootSaga() {
  yield all([
    ...UserSagas,
  ]);
}