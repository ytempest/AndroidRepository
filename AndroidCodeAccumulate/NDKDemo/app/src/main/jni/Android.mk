LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE    := file_crypt
LOCAL_SRC_FILES := file_crypt.c
include $(BUILD_SHARED_LIBRARY)


include $(CLEAR_VARS)
LOCAL_MODULE    := file_diff
LOCAL_SRC_FILES := file_diff.c
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)


include $(CLEAR_VARS)
LOCAL_MODULE    := posix_thread
LOCAL_SRC_FILES := posix_thread.c
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)
