import React from 'react';
import { Route, Switch } from 'react-router-dom';
import { useSelector } from 'react-redux';
import Admin from './components/admin';
import User from './components/user';
import Login from './components/login';
import SignUp from './components/signup';
import SignUpAdmin from './components/signup/signup.admin';

const App = () => {
  const auth = useSelector(state => state.user.auth);

  return (
    <div>
      <Switch>
        <Route exact path='/' component={auth === 'admin' ? Admin
          : auth === 'user' ? User : Login
        } />
        <Route exact path='/signup' component={SignUp} />
        <Route exact path='/signup/admin' component={SignUpAdmin} />
      </Switch>
    </div>
  );
}

export default App;