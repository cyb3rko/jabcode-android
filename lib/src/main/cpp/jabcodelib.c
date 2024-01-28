#include <jni.h>
#include <malloc.h>
#include "jabcode.h"

jbyteArray charArrayToByteArray(JNIEnv *env, const char* data, const int data_length) {
    jbyteArray array = (*env)->NewByteArray(env, data_length);
    jbyte *bytes = (*env)->GetByteArrayElements(env, array, 0);
    for (int i = 0; i < data_length; i++)
    {
        bytes[i] = data[i];
    }
    (*env)->SetByteArrayRegion(env, array, 0, data_length, bytes);
    (*env)->ReleaseByteArrayElements(env, array, bytes, 0);
    return array;
}

/**
 * @brief JABCode reader modified main function
 * @return jByteArray containing JABCode content
*/
jbyteArray detect(JNIEnv* env, char *path)
{
    //load image
    jab_bitmap* bitmap;
    bitmap = readImage(path);
    if(bitmap == NULL)
        return NULL;

    //find and decode JABCode in the image
    jab_int32 decode_status;
    jab_decoded_symbol symbols[MAX_SYMBOL_NUMBER];
    jab_data* decoded_data = decodeJABCodeEx(bitmap, NORMAL_DECODE, &decode_status, symbols, MAX_SYMBOL_NUMBER);
    if(decoded_data == NULL)
    {
        free(bitmap);
        reportError("Decoding JABCode failed");
        return NULL;
    }

    //output warning if the code is only partly decoded with COMPATIBLE_DECODE mode
    if(decode_status == 2)
    {
        JAB_REPORT_INFO(("The code is only partly decoded. Some slave symbols have not been decoded and are ignored."))
    }

    jbyteArray array = charArrayToByteArray(env, decoded_data->data, decoded_data->length);
    free(bitmap);
    free(decoded_data);
    return array;
}

JNIEXPORT jbyteArray JNICALL
Java_de_cyb3rko_jabcodelib_JabCodeLib_detect(
        JNIEnv* env,
        jobject thiz
) {
    return detect(env, "/data/user/0/de.cyb3rko.jabcodereader/cache/feed.png");
}
