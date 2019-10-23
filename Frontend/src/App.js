import React, { useEffect } from 'react';
import { Route, Switch } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import Admin from './components/admin';
import User from './components/user';
import Login from './components/login';
import SignUp from './components/signup';
import SignUpAdmin from './components/signup/signup.admin';
import { UrlAction } from './store/url/url.action'

const App = () => {
  const dispatch = useDispatch();
  const auth = useSelector(state => state.user.auth);

  useEffect(() => {
    dispatch(UrlAction.getRosemaryUrl.index());
    dispatch(UrlAction.getGwAppUrl.index());
  }, []);

  return (
    <div>
      <Switch>
        <Route exact path='/' component={auth === 'admin' ? Admin : auth === 'user' ? User : Login} />
        <Route exact path='/signup' component={SignUp} />
        <Route exact path='/signup/admin' component={SignUpAdmin} />
      </Switch>
    </div>
  );
}

export default App;