package huins.ex.proto;

import huins.ex.proto.connection.ConnectionResult;

interface IApiListener {

    int getApiVersionCode();

    oneway void onConnectionFailed(in ConnectionResult result);

    int getClientVersionCode();
}