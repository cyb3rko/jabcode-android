/*
 * Copyright (C) 2024 Cyb3rKo
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Lesser Public License for more
 * details.
 *
 * A copy of the GNU Lesser General Public License can be found alongside this
 * library's source code. Alternatively, see at <http://www.gnu.org/licenses/>.
 */

#include <jni.h>
#include <malloc.h>

#include "jabcode.h"

/**
 * @brief Helper function to convert char arrays to byte arrays
 * @param env the JNIEnv instance to allocate ByteArrays with
 * @param data the char array to be converted
 * @param data_length the char array length
 * @return jByteArray containing the char array content
 */
jbyteArray charArrayToByteArray(JNIEnv *env, const char *data, const int data_length) {
    jbyteArray array = (*env)->NewByteArray(env, data_length);
    jbyte *bytes = (*env)->GetByteArrayElements(env, array, 0);
    for (int i = 0; i < data_length; i++) {
        bytes[i] = data[i];
    }
    (*env)->SetByteArrayRegion(env, array, 0, data_length, bytes);
    (*env)->ReleaseByteArrayElements(env, array, bytes, 0);
    return array;
}

/**
 * @brief JABCode reader modified main function
 * @param env the JNIEnv instance to allocate byte arrays with
 * @param path the absolute path of the image to be analyzed
 * @return jByteArray containing JABCode content
 */
jbyteArray detect(JNIEnv *env, const char *path) {
    // load image
    jab_bitmap *bitmap;
    bitmap = readImage((jab_char *) path);
    if (bitmap == NULL) return NULL;

    // find and decode JABCode in the image
    jab_int32 decode_status;
    jab_decoded_symbol symbols[MAX_SYMBOL_NUMBER];
    jab_data *decoded_data =
        decodeJABCodeEx(bitmap, NORMAL_DECODE, &decode_status, symbols, MAX_SYMBOL_NUMBER);
    if (decoded_data == NULL) {
        free(bitmap);
        reportError("Decoding JABCode failed");
        return NULL;
    }

    // output warning if the code is only partly decoded with COMPATIBLE_DECODE
    // mode
    if (decode_status == 2) {
        JAB_REPORT_INFO(
            ("The code is only partly decoded. Some slave symbols have "
             "not been decoded and are ignored."))
    }

    jbyteArray array = charArrayToByteArray(env, decoded_data->data, decoded_data->length);
    free(bitmap);
    free(decoded_data);
    return array;
}

/**
 * @brief JNICall to the Kotlin 'detect' function
 * @param env the JNIEnv instance
 * @param thiz the calling Java object (unused)
 * @param imagePath the absolute path of the image to be analyzed
 * @return jByteArray containing JABCode content
 */
JNIEXPORT jbyteArray JNICALL Java_de_cyb3rko_jabcodelib_JabCodeLib_detect(JNIEnv *env,
                                                                          __attribute__((unused))
                                                                          jobject thiz,
                                                                          jstring imagePath) {
    return detect(env, (*env)->GetStringUTFChars(env, imagePath, JNI_FALSE));
}
