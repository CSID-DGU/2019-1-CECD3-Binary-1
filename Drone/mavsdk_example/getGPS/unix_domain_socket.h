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
struct gps_info_t{
    float altitude;
    double latitude;
    double longitude;
};


class unixDomainSocket {
private:
    int server_sockfd, client_sockfd;
    int state, client_len;
    struct sockaddr_un clientaddr, serveraddr;
    std::vector<gps_info_t> gps_info;
    std::mutex mutex_gps;
    std::mutex* mutex_atcion;
    std::condition_variable* cv;
    bool action_on;
    char file_name[24];
    char buf[255];
    void socketCreate();
    void socketAccept(int local_fd);
    void socketListen();


public:
    bool isActivate();
    void setGPS(float altitude, double latitude, double longitude);
    void actionOn();
    void actionOff();
    unixDomainSocket(std::string path, std::condition_variable* _cv, std::mutex* _mutex_action);
    ~unixDomainSocket() {
        close(client_sockfd);
    }
};

#endif //SOCKETTEST_UNIX_DOMAIN_SOCKET_H
