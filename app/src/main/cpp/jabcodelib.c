#include <jni.h>
#include "jabreader.h"

JNIEXPORT jint JNICALL
Java_de_cyb3rko_jabcodereader_MainActivity_detect(
        JNIEnv *env,
        jobject thiz) {
    char *argv[] = {"jabcodeReader", "/data/user/0/de.cyb3rko.jabcodereader/cache/feed.png"};
    return main(2, argv);
}
