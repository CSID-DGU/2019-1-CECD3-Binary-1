cmake_minimum_required(VERSION 2.8.12)

project(drone_service)

if(NOT MSVC)
    add_definitions("-std=c++11 -Wall -Wextra -Werror")
else()
    add_definitions("-std=c++11 -WX -W2")
endif()

find_package(MAVSDK REQUIRED)

add_executable(drone_service
        main.cpp unix_domain_socket.cpp unix_domain_socket.h
        drone_control.cpp drone_control.h main.h)

target_link_libraries(drone_service
    MAVSDK::mavsdk_info
    MAVSDK::mavsdk_telemetry
    MAVSDK::mavsdk_action
    MAVSDK::mavsdk_mission
    MAVSDK::mavsdk
    pthread
)
