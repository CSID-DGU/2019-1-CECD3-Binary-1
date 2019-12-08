# Installation Guide For Debian-linux OS

git clone https://github.com/mavlink/MAVSDK.git  #clone source code
cd MAVSDK

git checkout master
# git checkout develop
# develop for latest version

git submodule update --init --recursive #install submodules

cmake -DCMAKE_BUILD_TYPE=Debug -DBUILD_SHARED_LIBS=ON -Bbuild/default -H.
cmake --build build/default
sudo cmake --build build/default --target install # sudo is required to install to system directories!
sudo ldconfig  # update linker cache