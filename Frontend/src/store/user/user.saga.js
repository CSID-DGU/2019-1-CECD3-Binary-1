import { call, put, takeLatest } from 'redux-saga/effects';
import { SHA256 } from 'crypto-js';
import { UserAction, UserActionTypes } from './user.action';
import { POST } from '../../utils/Api';

export function* login(action) {
  try {
    yield put(UserAction.login.request());
    const response = yield call(POST, '/login', { ...action.payload, pw: SHA256(action.payload).toString() });
    if (response.data.auth === 'admin' || response.data.auth === 'user') {
      yield put(UserAction.login.success({ ...action.payload, auth: response.data.auth, name: response.data.name }));
    }
    else alert('아이디 또는 비밀번호를 확인해주세요.');
  } catch (error) {
    yield put(UserAction.login.failure(error));
  }
}

export function* logout() {
  try {
    yield put(UserAction.logout.request());
    yield put(UserAction.logout.success());
  } catch (error) {
    yield put(UserAction.logout.failure());
  }
}

export function* signUp(action) {
  try {
    yield put(UserAction.signUp.request());
    const response = yield call(POST, '/users', { ...action.payload, pw: SHA256(action.payload).toString() });
    yield put(UserAction.signUp.success(response.data));
  } catch (error) {
    yield put(UserAction.signUp.failure(error));
  }
}

export function* signUpAdmin(action) {
  try {
    yield put(UserAction.signUpAdmin.request());
    const response = yield call(POST, '/users/admin', { ...action.payload, pw: SHA256(action.payload).toString() });
    yield put(UserAction.signUpAdmin.success(response.data));
  } catch (error) {
    yield put(UserAction.signUpAdmin.failure(error));
  }
}

export const UserSagas = [
  takeLatest(UserActionTypes.LOGIN.INDEX, login),
  takeLatest(UserActionTypes.LOGOUT.INDEX, logout),
  takeLatest(UserActionTypes.SIGN_UP.INDEX, signUp),
  takeLatest(UserActionTypes.SIGN_UP_ADMIN.INDEX, signUpAdmin),
];