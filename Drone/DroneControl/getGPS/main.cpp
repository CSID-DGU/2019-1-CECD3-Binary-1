#include "main.h"

using namespace mavsdk;
using namespace std::this_thread;
using namespace std::chrono;

void usage(std::string bin_name)
{
    std::cout << NORMAL_CONSOLE_TEXT << "Usage : " << bin_name << " <connection_url>" << std::endl
              << "Connection URL format should be :" << std::endl
              << " For TCP : tcp://[server_host][:server_port]" << std::endl
              << " For UDP : udp://[bind_host][:bind_port]" << std::endl
              << " For Serial : serial:///path/to/serial/dev[:baudrate]" << std::endl
              << "For example, to connect to the simulator use URL: udp://:14540" << std::endl;
}

void component_discovered(ComponentType component_type)
{
    std::cout << NORMAL_CONSOLE_TEXT << "Discovered a component with type "
              << unsigned(component_type) << std::endl;
}

int main(int argc, char** argv)
{
    Mavsdk dc;
    std::string connection_url;
    ConnectionResult connection_result;
    std::condition_variable cv;
    std::mutex mutex_action;
    int mode;

    bool discovered_system = false;
    if (argc == 2) {
        connection_url = argv[1];
        connection_result = dc.add_any_connection(connection_url);
    } else {
        usage(argv[0]);
        return 1;
    }

    if (connection_result != ConnectionResult::SUCCESS) {
        std::cout << ERROR_CONSOLE_TEXT
                  << "Connection failed: " << connection_result_str(connection_result)
                  << NORMAL_CONSOLE_TEXT << std::endl;
        return 1;
    }

    // We don't need to specify the UUID if it's only one system anyway.
    // If there were multiple, we could specify it with:
    // dc.system(uint64_t uuid);
    System& system = dc.system();

    std::cout << "Waiting to discover system..." << std::endl;
    dc.register_on_discover([&discovered_system](uint64_t uuid) {
        std::cout << "Discovered system with UUID: " << uuid << std::endl;
        discovered_system = true;
    });

    // We usually receive heartbeats at 1Hz, therefore we should find a syste교m after around 2
    // seconds.
    
    sleep_for(seconds(2));
    
    if (!discovered_system) {
        std::cout << ERROR_CONSOLE_TEXT << "No system found, exiting." << NORMAL_CONSOLE_TEXT
                  << std::endl;
        return 1;
    }

    // Register a callback so we get told when components (camera, gimbal) etc
    // are found.
    system.register_component_discovered_callback(component_discovered);


    //add telemetry, action, mission pointer to droneControl class

    auto action = std::make_shared<Action>(system);
    auto mission = std::make_shared<Mission>(system);
    auto telemetry = std::make_shared<Telemetry>(system);
    auto follow_me = std::make_shared<FollowMe>(system);
    unixDomainSocket sock("/tmp/unix.sock", &cv, &mutex_action, &mode);    //make socket
    droneControl cont(action, mission, telemetry, follow_me, &mode);
    // We want to listen to the GPS and alititude of the drone at 1 Hz.
    {
        std::cout << "Setting rate updates..." << std::endl;

        auto prom = std::make_shared<std::promise<Telemetry::Result>>();
        auto future_result = prom->get_future();
        // Set position update rate to 1 Hz.
        telemetry->set_rate_position_async(1.0, [prom](Telemetry::Result result) {
            prom->set_value(result); //fulfill promise
        });

        //Block until promise is fulfilled (in callback function)
        const Telemetry::Result result = future_result.get();
        if (result != Telemetry::Result::SUCCESS) {
            // handle rate-setting failure (in this case print error)
            std::cout << "Setting telemetry rate failed (" << Telemetry::result_str(result) << ")." << std::endl;
        }
        
    }
    
    const Telemetry::Result set_rate_result = telemetry->set_rate_position(1.0);
    if (set_rate_result != Telemetry::Result::SUCCESS) {
        std::cout << ERROR_CONSOLE_TEXT
                  << "Setting rate failed:" << Telemetry::result_str(set_rate_result)
                  << NORMAL_CONSOLE_TEXT << std::endl;
        return 1;
    }

    // Set up callback to monitor altitude while the vehicle is in flight
    telemetry->position_async([](Telemetry::Position position) {
        std::cout << TELEMETRY_CONSOLE_TEXT // set to blue
                  << "Altitude: " << position.relative_altitude_m << " m"
                  << NORMAL_CONSOLE_TEXT // set to default color again
                  << std::endl;
#ifdef SOCKET_ON
        sock.setGPS(position.relative_altitude_m, position.latitude_deg, position.longitude_deg);
#endif
        std::cout << "Altitude: " << position.relative_altitude_m << " m" << std::endl
                << "Latitude: " << position.latitude_deg << std::endl
                << "Longitude: " << position.longitude_deg << std::endl;
    });
    // Check if vehicle is ready to arm
    while (telemetry->health_all_ok() != true) {
        std::cerr << "Vehicle is getting ready to arm" << std::endl;
        std::cerr << telemetry->health() << std::endl;
        sleep_for(seconds(1));
    }

    int i = 0;
    while (i < 5) {
        std::unique_lock<std::mutex> lk(mutex_action);
        cv.wait(
                lk, [&] { return sock.isActivate(); });
        switch(mode) {
            case TEST:
                cont.testTakeoff();
                break;
            case PATROL:
                if (cont.patrol() == GO_LOC) {

                }
                else
                    break;
            case GO_LOC:

                break;
            case RTL:

                break;
            default:
                std::cout << "Error on Command!" << std::endl;
        }

        mode = INACTIVE;
        std::this_thread::sleep_for(std::chrono::milliseconds(80));
    }

    
    
    
    return 0;
}
