//
// Created by Administrator on 2020/11/19.
//

#include "JtoolUtils.h"

char * JtoolUtils::jarray2charponit(JNIEnv *env, jbyteArray array) {
    char *chars = NULL;
    jbyte *bytes;
    bytes = env->GetByteArrayElements(array, 0);
    int chars_len = env->GetArrayLength(array);
    this->charLen = chars_len * sizeof(char);
    chars = (char *)malloc(this->charLen);
    memcpy(chars, bytes, chars_len);
    env->ReleaseByteArrayElements(array, bytes, 0);
    return chars;
}

jbyteArray JtoolUtils::charpoint2jarray(JNIEnv *env, char *data) {
    jbyteArray ja =env->NewByteArray(this->charLen);
    env->SetByteArrayRegion(ja,0,this->charLen, (jbyte*)data);
    env->ReleaseByteArrayElements(ja,env->GetByteArrayElements(ja,JNI_FALSE), 0);
    return ja;
}