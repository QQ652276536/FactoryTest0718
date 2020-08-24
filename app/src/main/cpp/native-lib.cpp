#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_zistone_myapplication_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_zistone_factorytest0718_TestTestActivity_stringFromJNI(JNIEnv *env, jobject thiz) {
    std::string hello = "寒光照铁衣";
    return env->NewStringUTF(hello.c_str());
}