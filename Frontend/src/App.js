import React from 'react';
import { Route, Switch } from 'react-router-dom';
import { useSelector } from 'react-redux';
import Admin from './components/admin';
import User from './components/user';
import Login from './components/login';
import SignUp from './components/signup';

const App = () => {
  const isLoggedIn = useSelector(state => state.user.isLoggedIn);

  return (
    <div>
      <Switch>
        <Route exact path='/' component={isLoggedIn === 'admin' ? Admin
          : isLoggedIn === 'user' ? User : Login
        } />
        <Route exact path='/signup' component={SignUp} />
      </Switch>
    </div>
  );
}

export default App;