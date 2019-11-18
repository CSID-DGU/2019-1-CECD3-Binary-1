#include <cstdio>
#include <signal.h>
#include <iostream>
namespace
{
volatile sig_atomic_t quitok = false;
void handle_break(int a)
{
    if (a == SIGINT) {
        std::cout << "get Signal inturrupt";
        exit(1);
    }
}
} // namespace

int main()
{
    struct sigaction sigbreak;
    sigbreak.sa_handler = &handle_break;
    sigemptyset(&sigbreak.sa_mask);
    sigbreak.sa_flags = 0;
    if (sigaction(SIGINT, &sigbreak, NULL) != 0)
        std::perror("sigaction");
    while(1){

    }
}