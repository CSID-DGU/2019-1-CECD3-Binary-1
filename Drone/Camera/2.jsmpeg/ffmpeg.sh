#!/bin/bash
ffmpeg -f v4l2 -framerate 15 -video_size 1280x720 -i /dev/video1 -f mpegts -codec:v mpeg1video -s 640x480 -b:v 1000k -bf 0 http://localhost:8081/supersecret

#Using v4l2 function
#input video from /dev/video0
#codec : mpeg1video
#Stream
