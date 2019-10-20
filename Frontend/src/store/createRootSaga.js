import { all } from 'redux-saga/effects';
import { UserSagas } from './user/user.saga';
import { UrlSagas } from './url/url.saga';

export default function* createRootSaga() {
  yield all([
    ...UserSagas,
    ...UrlSagas,
  ]);
}