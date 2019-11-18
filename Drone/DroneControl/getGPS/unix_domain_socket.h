//
// Created by Choi on 2019-11-11.
//

#ifndef SOCKETTEST_UNIX_DOMAIN_SOCKET_H
#define SOCKETTEST_UNIX_DOMAIN_SOCKET_H

#include <sys/types.h>
#include <sys/stat.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <unistd.h>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <iostream>
#include <thread>
#include <vector>
#include <mutex>
#include <condition_variable>
#include "main.h"



class unixDomainSocket {
private:
    int server_sockfd, client_sockfd;
    int state, client_len;
    int* mode;
    struct sockaddr_un clientaddr, serveraddr;
    std::vector<gps_info_t> gps_info;
    std::mutex mutex_gps;
    std::mutex* mutex_atcion;
    std::condition_variable* cv;
    char file_name[24];
    char buf[255];
    void socketCreate();
    void socketAccept(int local_fd);
    void socketListen();
    std::thread* thread_{nullptr};
    std::atomic<bool> call_exit_;

    void follow_person_start(){

    }
    void follow_person_stop();


public:
    typedef std::function<void(double lat, double lon)> location_callback_t;    //location call_back
    void request_location_updates(location_callback_t callback);
    bool is_running() { return !call_exit_; };
    void requestLocationUpdate() {
        location_callback_ = callback;
        stop();
        start();
    }
    bool isActivate();
    void setGPS(float altitude, double latitude, double longitude);
    void actionOn();
    void actionOff();
    unixDomainSocket(std::string path, std::condition_variable* _cv, std::mutex* _mutex_action, int* _mode);
    ~unixDomainSocket() { close(client_sockfd); }
};

#endif //SOCKETTEST_UNIX_DOMAIN_SOCKET_H
