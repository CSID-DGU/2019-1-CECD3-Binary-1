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
#include <atomic>
#include <functional>
#include <sstream>
#include "main.h"



class unixDomainSocket {
private:

    typedef std::function<void(double lat, double lon)> location_callback_t;    //location call_back
    //for socket
    int server_sockfd, client_sockfd;
    int state, client_len;
    char file_name[24];
    char buf[255];
    bool activate = false;
    //for communicate with main thread
    int* mode;
    struct sockaddr_un clientaddr, serveraddr;
    std::vector<gps_info_t> gps_info;
    std::queue<gps_info_t> follow_gps_info;
    //for pthread_mutex;
    std::mutex mutex_gps;
    std::mutex* mutex_atcion;
    std::condition_variable* cv;

    //for follow mode
    std::thread* thread_{nullptr};
    std::atomic<bool> follow_end{false};
    location_callback_t location_callback = nullptr;


    //socket
    void socketCreate();
    void socketAccept(int local_fd);
    void socketListen();

    //follow mode
    void followStart();
    void followStop();


public:

    void requestLocationUpdate(location_callback_t callback);
    void getLocations();
    bool isFollowRunning() { return !follow_end; };

    bool isActivate();
    void setGPS(float altitude, double latitude, double longitude);
    void actionOn();
    void actionOff();
    unixDomainSocket(std::string path, std::condition_variable* _cv, std::mutex* _mutex_action, int* _mode);
    ~unixDomainSocket() { close(client_sockfd); }
};

#endif //SOCKETTEST_UNIX_DOMAIN_SOCKET_H
