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
            if (strncmp(buf +5, "test", 4)) {
                if (!action_on) {
                    mutex_atcion->lock();
                    //set condition variable to enable
                    action_on = true;
                    mutex_atcion->unlock();
                    cv->notify_one();
                } else {
                    write(local_fd, "ERROR", 5);
                }
            }
            else if (strncmp(buf +5, "patrol", 6)) {        //patrol : using mavsdk fly mission

            }
            else if (strncmp(buf +5, "drone", 5)) {         //cal drone

            }

        }
        else if (strncmp(buf, "call_test", 9) == 0){
            if (!action_on) {
                mutex_atcion->lock();
                //set condition variable to enable
                action_on = true;
                std::cout << "getCommand : call_test" << std::endl;
                mode = TEST;
                mutex_atcion->unlock();
                cv->notify_one();
            }
            else {
                write(local_fd, "ERROR", 5);
            }
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
    while (1) {
        client_sockfd = accept(server_sockfd, (struct sockaddr *) &clientaddr, (socklen_t*)&client_len);
        printf("Connected!\n");

        //create Thread
        //todo 그 지금 보면 스레드가 쫌 좆같게돌아가는데, 어차피 하나만 연결될꺼면
        //여기서 따로 스레드 만들 필요는 없지않으려나 싶음
        std::thread mine([=](){socketAccept(client_sockfd);});
        mine.detach();
    }
}
void unixDomainSocket::setGPS(float altitude, double latitude, double longitude) {
    std::lock_guard<std::mutex> lock(mutex_gps);    //lock
    gps_info.push_back(gps_info_t{altitude, latitude, longitude});

}
void unixDomainSocket::actionOn() {
    action_on = true;
}
void unixDomainSocket::actionOff() {
    action_on = false;
}
bool unixDomainSocket::isActivate() {
    if (action_on) {
        return true;
    }
    return false;
}
unixDomainSocket::unixDomainSocket(std::string path, std::condition_variable* _cv, std::mutex* _mutex_action, flightMode& _mode){
    action_on = false;
    mode = _mode;
    cv = _cv;
    mutex_atcion = _mutex_action;
    strcpy(file_name, path.c_str());
    socketCreate();
    std::cout << "socket created" << std::endl;
    std::thread sock([&](){socketListen();});
    sock.detach();
}
