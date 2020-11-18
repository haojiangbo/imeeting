#include <jni.h>
#include <string>
#include "stdio.h"

extern "C" JNIEXPORT jstring JNICALL
Java_com_haojiangbo_ndkdemo_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    printf("test c++ \n");
    fflush(stdout);
    return env->NewStringUTF(hello.c_str());
}



extern "C"
JNIEXPORT jstring JNICALL
Java_com_haojiangbo_ndkdemo_MainActivity_message(JNIEnv *env, jobject thiz) {
    // TODO: implement message()

}