
LOCAL_PATH := $(call my-dir)

#ffmpeg lib
include $(CLEAR_VARS)
LOCAL_MODULE := avutil
LOCAL_SRC_FILES := libavutil-55.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := swresample
LOCAL_SRC_FILES := libswresample-2.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := avcodec
LOCAL_SRC_FILES := libavcodec-57.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := avformat
LOCAL_SRC_FILES := libavformat-57.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := swscale
LOCAL_SRC_FILES := libswscale-4.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := postproc
LOCAL_SRC_FILES := libpostproc-54.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := avfilter
LOCAL_SRC_FILES := libavfilter-6.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := avdevice
LOCAL_SRC_FILES := libavdevice-57.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := yuv
LOCAL_SRC_FILES := libyuv.so
include $(PREBUILT_SHARED_LIBRARY)

# myapp
include $(CLEAR_VARS)
LOCAL_MODULE := media_play
LOCAL_SRC_FILES := media_play.c audio_play.c
LOCAL_C_INCLUDES += $(LOCAL_PATH)/ffmpeg
LOCAL_C_INCLUDES += $(LOCAL_PATH)/libyuv
##-landroid参数 for native windows
LOCAL_LDLIBS := -llog -landroid
LOCAL_SHARED_LIBRARIES := avutil swresample avcodec avformat swscale postproc avfilter avdevice yuv
include $(BUILD_SHARED_LIBRARY)


