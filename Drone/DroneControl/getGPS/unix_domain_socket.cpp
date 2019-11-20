#include "unix_domain_socket.h"
void unixDomainSocket::socketCreate() {
    // 주소 파일을 읽어들인다.
    client_len = sizeof(clientaddr);

    // internet 기반의 스트림 소켓을 만들도록 한다.
    if ((server_sockfd = socket(AF_UNIX, SOCK_STREAM, 0)) < 0) {
        std::cerr << "Socket error" << std::endl;
        exit(0);
    }
    bzero(&serveraddr, sizeof(serveraddr));
    serveraddr.sun_family = AF_UNIX;
    strcpy(serveraddr.sun_path, file_name);

    state = bind(server_sockfd, (struct sockaddr *) &serveraddr,
                 sizeof(serveraddr));
    if (state == -1) {
        std::cerr << "bind error : " << std::endl;
        exit(0);
    }
}
void unixDomainSocket::socketAccept(int local_fd) {
    if (local_fd == -1) {
        perror("Accept error : ");
        return;
    }
    while (1) {
        memset(buf, '\0', 255);
        if (read(local_fd, buf, 255) <= 0) {
            close(local_fd);
            //fclose(fp);
            break;
        }
        std::cout << "GET DATA : " << buf << std::endl;
        //이부분이 소켓으로 온거에 따라서 반응하는거임
        if (strncmp(buf, "quit", 4) == 0) {
            write(local_fd, "Disconnected\n", 8);
            close(local_fd);
            //fclose(fp);
            break;
        }
        else if (strncmp(buf, "get_", 4) == 0) {  //
            if (strncmp(buf + 4, "gps", 3)) {
                int last_index = gps_info.size() - 1;
                if (last_index >= 0) {
                    std::string gps_str = "/";  //start
                    gps_str += std::to_string((gps_info[last_index].altitude));
                    gps_str.push_back(' ');
                    gps_str += std::to_string((gps_info[last_index].longitude));
                    gps_str.push_back(' ');
                    gps_str += std::to_string((gps_info[last_index].latitude));
                    gps_str.push_back('/'); //end
                    write(local_fd, gps_str.c_str(), gps_str.size());
                    std::cout << "SEND DATA : " << gps_str << std::endl;
                } else {
                    write(local_fd, "Error : No GPS", 14);
                }
            }
        }
        else if (strncmp(buf, "call_", 5) == 0){
            if (!strncmp(buf +5, "test", 4)) {
                printf("%s", buf + 5);
                if (!(*mode)) {       //if mode inactive

                    //set condition variable to enable
                    mutex_atcion->lock();
                    *mode = TEST;
                    actionOn();
                    mutex_atcion->unlock();
                    cv->notify_one();
                } else {
                    write(local_fd, "ERROR", 5);
                }
            }
            else if (!strncmp(buf +5, "patrol", 6)) {        //patrol : using mavsdk fly mission
                if (!(*mode)) {       //if mdoe inactive
                    mutex_atcion->lock();
                    *mode = PATROL;
                    actionOn();
                    mutex_atcion->unlock();
                    cv->notify_one();
                } else {
                    write(local_fd, "Already on Action : Patrol", 26);
                }
            }
            else if (!strncmp(buf +5, "drone", 5)) {         //cal drone
                if (!(*mode)) {
                    mutex_atcion->lock();
                    *mode = GO_LOC;
                    actionOn();
                    mutex_atcion->unlock();
                    cv->notify_one();
                } else {    //stop current mission and goto follow me mode
                    //write(local_fd, "Already on Action : Patrol", 26);
                    *mode = GO_LOC;
                }
            }
            else if (!strncmp(buf +5, "rtl", 3)) {         //cal drone
                if (!(*mode)) {
                    //set condition variable to enable
                    mutex_atcion->lock();
                    *mode = RTL;
                    actionOn();
                    mutex_atcion->unlock();
                    cv->notify_one();
                } else {    //stop current mission and goto follow me mode
                    *mode = RTL;
                }
            }
        }
        else if (!strncmp(buf, "set_", 4)) {
            std::stringstream value_(buf + 4);
            std::string token_;
            value_ >> token_;
            double longitude_ = std::stoi(token_);
            value_ >> token_;
            double latitude_ = std::stoi(token_);
            follow_gps_info.push({0.f, latitude_, longitude_});

        }
    }
}
void unixDomainSocket::socketListen() {
    state = listen(server_sockfd, 5);
    if (state == -1) {
        perror("listen error : ");
        //exit(0);
    }
    printf("accept : \n");
    client_sockfd = accept(server_sockfd, (struct sockaddr *) &clientaddr, (socklen_t*)&client_len);
    printf("Connected!\n");

    //socket accept without creating socket
    //one socket id enough for this perpose
    socketAccept(client_sockfd);

}
void unixDomainSocket::setGPS(float altitude, double latitude, double longitude) {
    std::lock_guard<std::mutex> lock(mutex_gps);    //lock
    gps_info.push_back(gps_info_t{altitude, latitude, longitude});

}
void unixDomainSocket::actionOn() {
    activate = true;
}
void unixDomainSocket::actionOff() {
    activate = false;
}
bool unixDomainSocket::isActivate() {
    return activate; //todo change
}
void unixDomainSocket::requestLocationUpdate(location_callback_t callback)
{
    location_callback = callback;
    followStop();
    followStart();
}

void unixDomainSocket::followStart()
{
    follow_end = false;
    thread_ = new std::thread(&unixDomainSocket::getLocations, this);
}

void unixDomainSocket::followStop()
{
    follow_end = true;

    if (thread_) {
        thread_->join();
        delete thread_;
        thread_ = nullptr;
    }
}

// Rudimentary location provider to draw a square.
void unixDomainSocket::getLocations()
{
    while (!follow_end) {
        // 1. get data from socket
        if (!follow_gps_info.empty()) {
            location_callback(follow_gps_info.front().latitude, follow_gps_info.front().longitude);
            follow_gps_info.pop();
        }
        std::this_thread::sleep_for(std::chrono::seconds(1));

    }
}

unixDomainSocket::unixDomainSocket(std::string path, std::condition_variable* _cv, std::mutex* _mutex_action, int* _mode) {
    mode = _mode;
    cv = _cv;
    mutex_atcion = _mutex_action;
    strcpy(file_name, path.c_str());
    socketCreate();
    std::cout << "socket created" << std::endl;
    std::thread sock([&](){socketListen();});
    sock.detach();
}
