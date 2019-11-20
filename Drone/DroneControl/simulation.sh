#! /bin/bash

echo "Simulate using jmavsim"
export PX4_HOME_LAT=37.5562923
export PX4_HOME_LON=126.99995498
export PX4_HOME_ALT=28.5
export PX4_SIM_SPEED_FACTOR=2
make -C ~/src/Firmware/ px4_sitl jmavsim
