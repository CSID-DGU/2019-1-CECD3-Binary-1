# CMAKE generated file: DO NOT EDIT!
# Generated by "Unix Makefiles" Generator, CMake Version 3.13

# Delete rule output on recipe failure.
.DELETE_ON_ERROR:


#=============================================================================
# Special targets provided by cmake.

# Disable implicit rules so canonical targets will work.
.SUFFIXES:


# Remove some rules from gmake that .SUFFIXES does not remove.
SUFFIXES =

.SUFFIXES: .hpux_make_needs_suffix_list


# Suppress display of executed commands.
$(VERBOSE).SILENT:


# A target that is always out of date.
cmake_force:

.PHONY : cmake_force

#=============================================================================
# Set environment variables for the build.

# The shell in which to execute make rules.
SHELL = /bin/sh

# The CMake executable.
CMAKE_COMMAND = /usr/bin/cmake

# The command to remove a file.
RM = /usr/bin/cmake -E remove -f

# Escaping for special characters.
EQUALS = =

# The top-level source directory on which CMake was run.
CMAKE_SOURCE_DIR = /home/pi/MAVSDK/example/battery

# The top-level build directory on which CMake was run.
CMAKE_BINARY_DIR = /home/pi/MAVSDK/example/battery

# Include any dependencies generated for this target.
include CMakeFiles/battery.dir/depend.make

# Include the progress variables for this target.
include CMakeFiles/battery.dir/progress.make

# Include the compile flags for this target's objects.
include CMakeFiles/battery.dir/flags.make

CMakeFiles/battery.dir/battery.cpp.o: CMakeFiles/battery.dir/flags.make
CMakeFiles/battery.dir/battery.cpp.o: battery.cpp
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir=/home/pi/MAVSDK/example/battery/CMakeFiles --progress-num=$(CMAKE_PROGRESS_1) "Building CXX object CMakeFiles/battery.dir/battery.cpp.o"
	/usr/bin/c++  $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -o CMakeFiles/battery.dir/battery.cpp.o -c /home/pi/MAVSDK/example/battery/battery.cpp

CMakeFiles/battery.dir/battery.cpp.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing CXX source to CMakeFiles/battery.dir/battery.cpp.i"
	/usr/bin/c++ $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -E /home/pi/MAVSDK/example/battery/battery.cpp > CMakeFiles/battery.dir/battery.cpp.i

CMakeFiles/battery.dir/battery.cpp.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling CXX source to assembly CMakeFiles/battery.dir/battery.cpp.s"
	/usr/bin/c++ $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -S /home/pi/MAVSDK/example/battery/battery.cpp -o CMakeFiles/battery.dir/battery.cpp.s

# Object files for target battery
battery_OBJECTS = \
"CMakeFiles/battery.dir/battery.cpp.o"

# External object files for target battery
battery_EXTERNAL_OBJECTS =

battery: CMakeFiles/battery.dir/battery.cpp.o
battery: CMakeFiles/battery.dir/build.make
battery: /usr/local/lib/libmavsdk_telemetry.so
battery: /usr/local/lib/libmavsdk.so
battery: CMakeFiles/battery.dir/link.txt
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --bold --progress-dir=/home/pi/MAVSDK/example/battery/CMakeFiles --progress-num=$(CMAKE_PROGRESS_2) "Linking CXX executable battery"
	$(CMAKE_COMMAND) -E cmake_link_script CMakeFiles/battery.dir/link.txt --verbose=$(VERBOSE)

# Rule to build all files generated by this target.
CMakeFiles/battery.dir/build: battery

.PHONY : CMakeFiles/battery.dir/build

CMakeFiles/battery.dir/clean:
	$(CMAKE_COMMAND) -P CMakeFiles/battery.dir/cmake_clean.cmake
.PHONY : CMakeFiles/battery.dir/clean

CMakeFiles/battery.dir/depend:
	cd /home/pi/MAVSDK/example/battery && $(CMAKE_COMMAND) -E cmake_depends "Unix Makefiles" /home/pi/MAVSDK/example/battery /home/pi/MAVSDK/example/battery /home/pi/MAVSDK/example/battery /home/pi/MAVSDK/example/battery /home/pi/MAVSDK/example/battery/CMakeFiles/battery.dir/DependInfo.cmake --color=$(COLOR)
.PHONY : CMakeFiles/battery.dir/depend

