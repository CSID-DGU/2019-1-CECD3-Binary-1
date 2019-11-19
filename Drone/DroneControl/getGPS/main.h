//
// Created by choi on 19. 11. 18.
//

#ifndef DRONE_SERVICE_MAIN_H
#define DRONE_SERVICE_MAIN_H
#include <chrono>
#include <cstdint>
#include <mavsdk/mavsdk.h>
#include <mavsdk/plugins/action/action.h>
#include <mavsdk/plugins/telemetry/telemetry.h>
#include <mavsdk/plugins/mission/mission.h>
#include <mavsdk/plugins/follow_me/follow_me.h>
#include <iostream>
#include <thread>
#include <future>
#include <queue>


#define ERROR_CONSOLE_TEXT "\033[31m" // Turn text on console red
#define TELEMETRY_CONSOLE_TEXT "\033[34m" // Turn text on console blue
#define NORMAL_CONSOLE_TEXT "\033[0m" // Restore normal console colour

struct gps_info_t{
    float altitude;
    double latitude;
    double longitude;
};
enum flightMode {
    INACTIVE,
    TEST,
    PATROL,
    GO_LOC,
    RTL
};

struct test{
    std::vector<gps_info_t> person_location;
    bool status;    //1 : active, 0 : inactive
};
#endif //DRONE_SERVICE_MAIN_H
