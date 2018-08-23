LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := audio_play
LOCAL_SRC_FILES := audio_play.cpp

# 引入外部模块的库，因为引入模块中的Android.mk构建的库名称是wavlib_static
LOCAL_STATIC_LIBRARIES += wavlib_static

# 表示引入OpenSL库和android的日志打印库
LOCAL_LDLIBS := -lOpenSLES -llog

include $(BUILD_SHARED_LIBRARY)

# 引入外部模块
# 添加要引用的模块的路径
$(call import-add-path,f:/transcode-1.1.7)
# 指定要引用的具体模块
$(call import-module,avilib)





