#include <cstdint>
#include <mavsdk/mavsdk.h>
#include <mavsdk/plugins/tune/tune.h>
#include <iostream>
#include <thread>

using namespace mavsdk;
using namespace std::this_thread;
using namespace std::chrono;

#define ERROR_CONSOLE_TEXT "\033[31m" // Turn text on console red
#define TELEMETRY_CONSOLE_TEXT "\033[34m" // Turn text on console blue
#define NORMAL_CONSOLE_TEXT "\033[0m" // Restore normal console colour

void usage(std::string bin_name)
{
    std::cout << NORMAL_CONSOLE_TEXT << "Usage : " << bin_name << " <connection_url>" << std::endl
              << "Connection URL format should be :" << std::endl
              << " For TCP : tcp://[server_host][:server_port]" << std::endl
              << " For UDP : udp://[bind_host][:bind_port]" << std::endl
              << " For Serial : serial:///path/to/serial/dev[:baudrate]" << std::endl
              << "For example, to connect to the simulator use URL: udp://:14540" << std::endl;
}

int main(int argc, char** argv)
{
    Mavsdk dc;
    std::string connection_url;
    ConnectionResult connection_result;

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

    // We usually receive heartbeats at 1Hz, therefore we should find a system after around 2
    // seconds.
    sleep_for(seconds(2));

    if (!discovered_system) {
        std::cout << ERROR_CONSOLE_TEXT << "No system found, exiting." << NORMAL_CONSOLE_TEXT
                  << std::endl;
        return 1;
    }

    std::vector<Tune::SongElement> the_tune;
    the_tune.push_back(Tune::SongElement::DURATION_4);
    the_tune.push_back(Tune::SongElement::NOTE_G);
    the_tune.push_back(Tune::SongElement::NOTE_A);
    the_tune.push_back(Tune::SongElement::NOTE_B);
    the_tune.push_back(Tune::SongElement::FLAT);
    the_tune.push_back(Tune::SongElement::OCTAVE_UP);
    the_tune.push_back(Tune::SongElement::DURATION_1);
    the_tune.push_back(Tune::SongElement::NOTE_E);
    the_tune.push_back(Tune::SongElement::FLAT);
    the_tune.push_back(Tune::SongElement::OCTAVE_DOWN);
    the_tune.push_back(Tune::SongElement::DURATION_4);
    the_tune.push_back(Tune::SongElement::NOTE_PAUSE);
    the_tune.push_back(Tune::SongElement::NOTE_F);
    the_tune.push_back(Tune::SongElement::NOTE_G);
    the_tune.push_back(Tune::SongElement::NOTE_A);
    the_tune.push_back(Tune::SongElement::OCTAVE_UP);
    the_tune.push_back(Tune::SongElement::DURATION_2);
    the_tune.push_back(Tune::SongElement::NOTE_D);
    the_tune.push_back(Tune::SongElement::NOTE_D);
    the_tune.push_back(Tune::SongElement::OCTAVE_DOWN);
    the_tune.push_back(Tune::SongElement::DURATION_4);
    the_tune.push_back(Tune::SongElement::NOTE_PAUSE);
    the_tune.push_back(Tune::SongElement::NOTE_E);
    the_tune.push_back(Tune::SongElement::FLAT);
    the_tune.push_back(Tune::SongElement::NOTE_F);
    the_tune.push_back(Tune::SongElement::NOTE_G);
    the_tune.push_back(Tune::SongElement::OCTAVE_UP);
    the_tune.push_back(Tune::SongElement::DURATION_1);
    the_tune.push_back(Tune::SongElement::NOTE_C);
    the_tune.push_back(Tune::SongElement::OCTAVE_DOWN);
    the_tune.push_back(Tune::SongElement::DURATION_4);
    the_tune.push_back(Tune::SongElement::NOTE_PAUSE);
    the_tune.push_back(Tune::SongElement::NOTE_A);
    the_tune.push_back(Tune::SongElement::OCTAVE_UP);
    the_tune.push_back(Tune::SongElement::NOTE_C);
    the_tune.push_back(Tune::SongElement::OCTAVE_DOWN);
    the_tune.push_back(Tune::SongElement::NOTE_B);
    the_tune.push_back(Tune::SongElement::FLAT);
    the_tune.push_back(Tune::SongElement::DURATION_2);
    the_tune.push_back(Tune::SongElement::NOTE_G);

    Tune tune(system);
    tune.play_tune_async(the_tune, 200, [](const Tune::Result result) {
        std::cout << NORMAL_CONSOLE_TEXT << "Tune sent with result: " << Tune::result_str(result)
                  << std::endl;
    });

    return 0;
}
