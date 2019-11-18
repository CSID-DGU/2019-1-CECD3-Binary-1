//
// Created by choi on 19. 11. 18.
//

#ifndef DRONE_SERVICE_DRONE_CONTROL_H
#define DRONE_SERVICE_DRONE_CONTROL_H

#include <iostream>
#include <mavsdk/mavsdk.h>
#include <mavsdk/plugins/action/action.h>
#include <mavsdk/plugins/telemetry/telemetry.h>
#include <mavsdk/plugins/mission/mission.h>
#include "main.h"

using namespace mavsdk;
using namespace std::this_thread;
using namespace std::chrono;

class droneControl{
private:
    std::shared_ptr<Action> action;
    std::shared_ptr<Mission> mission;
    std::shared_ptr<Telemetry> telemetry;
    std::string patrol_route;
    inline void handle_action_err_exit(Action::Result result, const std::string& message);
    inline void handle_mission_err_exit(Mission::Result result, const std::string& message);


// Handles connection result
    inline void handle_connection_err_exit(ConnectionResult result, const std::string& message);
public:
    droneControl(std::shared_ptr<Action> _action, std::shared_ptr<Mission> _mission, std::shared_ptr<Telemetry> _telemetry) {
        action = _action;
        mission = _mission;
        telemetry = _telemetry;
        patrol_route = "../patrol_route/test_route.plan";
    }
    int testTakeoff();
    int patrol();

};

#endif //DRONE_SERVICE_DRONE_CONTROL_H