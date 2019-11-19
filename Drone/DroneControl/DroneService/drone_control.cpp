//
// Created by choi on 19. 11. 18.
//

#include "drone_control.h"

// Handles Action's result
inline void droneControl::action_error_exit(Action::Result result, const std::string &message) {
    if (result != Action::Result::SUCCESS) {
        std::cerr << ERROR_CONSOLE_TEXT << message << Action::result_str(result)
                  << NORMAL_CONSOLE_TEXT << std::endl;
        exit(EXIT_FAILURE);
    }
}
// Handles FollowMe's result

inline void droneControl::follow_me_error_exit(FollowMe::Result result, const std::string &message) {
    if (result != FollowMe::Result::SUCCESS) {
        std::cerr << ERROR_CONSOLE_TEXT << message << FollowMe::result_str(result)
                  << NORMAL_CONSOLE_TEXT << std::endl;
        exit(EXIT_FAILURE);
    }
}
// Handles connection result

inline void droneControl::connection_error_exit(ConnectionResult result, const std::string &message) {
    if (result != ConnectionResult::SUCCESS) {
        std::cerr << ERROR_CONSOLE_TEXT << message << connection_result_str(result)
                  << NORMAL_CONSOLE_TEXT << std::endl;
        exit(EXIT_FAILURE);
    }
}


inline void droneControl::handle_action_err_exit(Action::Result result, const std::string &message) {
    if (result != Action::Result::SUCCESS) {
        std::cerr << ERROR_CONSOLE_TEXT << message << Action::result_str(result)
                  << NORMAL_CONSOLE_TEXT << std::endl;
        exit(EXIT_FAILURE);
    }
}

inline void droneControl::handle_mission_err_exit(Mission::Result result, const std::string &message) {
    if (result != Mission::Result::SUCCESS) {
        std::cerr << ERROR_CONSOLE_TEXT << message << Mission::result_str(result)
                  << NORMAL_CONSOLE_TEXT << std::endl;
        exit(EXIT_FAILURE);
    }
}

inline void droneControl::handle_connection_err_exit(ConnectionResult result, const std::string &message) {
    if (result != ConnectionResult::SUCCESS) {
        std::cerr << ERROR_CONSOLE_TEXT << message << connection_result_str(result)
                  << NORMAL_CONSOLE_TEXT << std::endl;
        exit(EXIT_FAILURE);
    }
}

int droneControl::testTakeoff() {
    std::cout << "Before Arming......" << std::endl;
    // Arm vehicle
    std::cout << "Arming..." << std::endl;
    const Action::Result arm_result = action->arm();

    if (arm_result != Action::Result::SUCCESS) {
        std::cout << ERROR_CONSOLE_TEXT << "Arming failed:" << Action::result_str(arm_result)
                  << NORMAL_CONSOLE_TEXT << std::endl;
        return 1;
    }

    // Take off
    std::cout << "Taking off..." << std::endl;
    const Action::Result takeoff_result = action->takeoff();
    if (takeoff_result != Action::Result::SUCCESS) {
        std::cout << ERROR_CONSOLE_TEXT << "Takeoff failed:" << Action::result_str(takeoff_result)
                  << NORMAL_CONSOLE_TEXT << std::endl;
        return 1;
    }

    // Let it hover for a bit before landing again.
    sleep_for(seconds(10));

    std::cout << "Landing..." << std::endl;
    const Action::Result land_result = action->land();
    if (land_result != Action::Result::SUCCESS) {
        std::cout << ERROR_CONSOLE_TEXT << "Land failed:" << Action::result_str(land_result)
                  << NORMAL_CONSOLE_TEXT << std::endl;
        return 1;
    }

    // Check if vehicle is still in air
    while (telemetry->in_air()) {
        std::cout << "Vehicle is landing..." << std::endl;
        sleep_for(seconds(1));
    }
    std::cout << "Landed!" << std::endl;

    // We are relying on auto-disarming but let's keep watching the telemetry for a bit longer.
    sleep_for(seconds(3));
    std::cout << "Finished..." << std::endl;
    return 0;
}

int droneControl::patrol() {
    // Import Mission items from QGC plan
    Mission::mission_items_t mission_items;
    Mission::Result import_res = Mission::import_qgroundcontrol_mission(mission_items, patrol_route);
    handle_mission_err_exit(import_res, "Failed to import mission items: ");

    if (mission_items.size() == 0) {
        std::cerr << "No patrol route! Exiting..." << std::endl;
        exit(EXIT_FAILURE);
    }
    std::cout << "Found " << mission_items.size() << " mission items in the given QGC plan."
              << std::endl;

    {
        std::cout << "Uploading route..." << std::endl;
        // Wrap the asynchronous upload_mission function using std::future.
        auto prom = std::make_shared<std::promise<Mission::Result>>();
        auto future_result = prom->get_future();
        mission->upload_mission_async(
                mission_items, [prom](Mission::Result result) { prom->set_value(result); });

        const Mission::Result result = future_result.get();
        handle_mission_err_exit(result, "Route upload failed: ");
        std::cout << "Route uploaded." << std::endl;
    }

    std::cout << "Arming..." << std::endl;
    const Action::Result arm_result = action->arm();
    handle_action_err_exit(arm_result, "Arm failed: ");
    std::cout << "Armed." << std::endl;

    // Before starting the mission subscribe to the mission progress.
    mission->subscribe_progress([](int current, int total) {
        std::cout << "Mission status update: " << current << " / " << total << std::endl;
    });

    {
        std::cout << "Starting mission." << std::endl;
        auto prom = std::make_shared<std::promise<Mission::Result>>();
        auto future_result = prom->get_future();
        mission->start_mission_async([prom](Mission::Result result) {
            prom->set_value(result);
            std::cout << "Started mission." << std::endl;
        });

        const Mission::Result result = future_result.get();
        handle_mission_err_exit(result, "Mission start failed: ");
    }


    while (!mission->mission_finished()) {
        if (*mode == GO_LOC) {  //change mode to GO_LOC!!!
            // 1. stop current mission
            {
                auto prom = std::make_shared<std::promise<Mission::Result>>();
                auto future_result = prom->get_future();

                std::cout << "Pausing mission..." << std::endl;
                mission->pause_mission_async(
                        [prom](Mission::Result result) {
                            prom->set_value(result);
                        });

                const Mission::Result result = future_result.get();
                if (result != Mission::Result::SUCCESS) {
                    std::cout << "Failed to pause mission (" << Mission::result_str(result) << ")" << std::endl;
                } else {
                    std::cout << "Mission paused." << std::endl;
                }
            }
            return GO_LOC;
        } else if(*mode == RTL) {
            {
                auto prom = std::make_shared<std::promise<Mission::Result>>();
                auto future_result = prom->get_future();

                std::cout << "Pausing mission..." << std::endl;
                mission->pause_mission_async(
                        [prom](Mission::Result result) {
                            prom->set_value(result);
                        });

                const Mission::Result result = future_result.get();
                if (result != Mission::Result::SUCCESS) {
                    std::cout << "Failed to pause mission (" << Mission::result_str(result) << ")" << std::endl;
                } else {
                    std::cout << "Mission paused." << std::endl;
                }
            }
            return RTL;
        } else
            sleep_for(seconds(1));
    }

    // Wait for some time.
    sleep_for(seconds(5));

    {
        // Mission complete. Command RTL to go home.
        std::cout << "Commanding Return to launch..." << std::endl;
        const Action::Result result = action->return_to_launch();
        if (result != Action::Result::SUCCESS) {
            std::cout << "Failed to command return to launch (" << Action::result_str(result) << ")"
                      << std::endl;
        } else {
            std::cout << "Commanded return to launch." << std::endl;
        }
    }
    return 0;
}

int droneControl::followPerson(unixDomainSocket& sock) {
    // Subscribe to receive updates on flight mode. You can find out whether FollowMe is active.
    telemetry->flight_mode_async(std::bind(
            [&](Telemetry::FlightMode flight_mode) {
                const FollowMe::TargetLocation last_location = follow_person->get_last_location();
                std::cout << "[FlightMode: " << Telemetry::flight_mode_str(flight_mode)
                          << "] Vehicle is at: " << last_location.latitude_deg << ", "
                          << last_location.longitude_deg << " degrees." << std::endl;
            },
            std::placeholders::_1));
    // Configure Min height of the drone to be "20 meters" above home & Follow direction as "Front
    // right".
    FollowMe::Config config;
    config.min_height_m = 10.0;
    config.follow_direction = FollowMe::Config::FollowDirection::BEHIND;
    FollowMe::Result follow_me_result = follow_person->set_config(config);

    // Start Follow Me
    follow_me_result = follow_person->start();
    follow_me_error_exit(follow_me_result, "Failed to start Follow mode");

    // Register for platform-specific Location provider. We're using FakeLocationProvider for the
    // example.

    sock.requestLocationUpdate([&](double lat, double lon) {
        follow_person->set_target_location({lat, lon, 0.0, 0.f, 0.f, 0.f});
    });

    while (sock.isFollowRunning()) {
        sleep_for(seconds(1));
    }

    // Stop Follow Me
    follow_me_result = follow_person->stop();
    follow_me_error_exit(follow_me_result, "Failed to stop FollowMe mode");

    // Stop flight mode updates.
    telemetry->flight_mode_async(nullptr);

    // Land
    const Action::Result land_result = action->land();
    action_error_exit(land_result, "Landing failed");
    while (telemetry->in_air()) {
        std::cout << "waiting until landed" << std::endl;
        sleep_for(seconds(1));
    }
    std::cout << "Landed..." << std::endl;
    return 0;
}

int droneControl::returnToLaunch() {
    // Wait for some time.
    sleep_for(seconds(5));

    {
        // Mission complete. Command RTL to go home.
        std::cout << "Commanding Return to launch..." << std::endl;
        const Action::Result result = action->return_to_launch();
        if (result != Action::Result::SUCCESS) {
            std::cout << "Failed to command return to launch (" << Action::result_str(result) << ")"
                      << std::endl;
        } else {
            std::cout << "Commanded return to launch." << std::endl;
        }
    }
    return 0;
}
