LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := andfix
LOCAL_SRC_FILES := andfix.cpp
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)


