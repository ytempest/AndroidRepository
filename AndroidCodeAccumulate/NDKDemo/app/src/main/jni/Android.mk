LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := ndk_file_crypt
LOCAL_SRC_FILES := ndk_file_crypt.c

include $(BUILD_SHARED_LIBRARY)
