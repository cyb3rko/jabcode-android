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

jab_data *data = 0;
jab_char *filename = 0;
jab_int32 color_number = 0;
jab_int32 symbol_number = 0;
jab_int32 module_size = 0;
jab_int32 master_symbol_width = 0;
jab_int32 master_symbol_height = 0;
jab_int32 *symbol_positions = 0;
jab_int32 symbol_positions_number = 0;
jab_vector2d *symbol_versions = 0;
jab_int32 symbol_versions_number = 0;
jab_int32 *symbol_ecc_levels = 0;
jab_int32 color_space = 0;
jclass optionsClass;

struct {
    const char *integer;
} JAVA_TYPES = {"I"};

/**
 * @brief Helper function to fetch a field ID of a Java object
 */
jfieldID getJavaFieldId(JNIEnv *env, jobject object, const char *fieldName, const char *fieldType) {
    if (optionsClass == NULL) optionsClass = (*env)->GetObjectClass(env, object);
    jfieldID fid = (*env)->GetFieldID(env, optionsClass, fieldName, fieldType);
    return fid;
}

/**
 * @brief Helper function to fetch an integer attribute of a Java object
 */
int getJavaIntField(JNIEnv *env, jobject object, const char *fieldName, const char *fieldType) {
    jfieldID fid = getJavaFieldId(env, object, fieldName, fieldType);
    return (*env)->GetIntField(env, object, fid);
}

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
 * @brief Free allocated buffers
 */
void cleanMemory() {
    free(data);
    free(symbol_positions);
    free(symbol_versions);
    free(symbol_ecc_levels);
}

/**
 * @brief Parse generation parameters
 * @return 1: success | 0: failure
 */
jab_boolean parseCommandLineParameters(JNIEnv *env, const char *sourcePath, const char *imagePath,
                                       jobject options) {
    // --input logic
    FILE *fp = fopen(sourcePath, "rb");
    if (!fp) {
        reportError("Opening input data file failed");
        return 0;
    }
    jab_int32 file_size;
    fseek(fp, 0, SEEK_END);
    file_size = ftell(fp);
    if (data) free(data);
    data = (jab_data *) malloc(sizeof(jab_data) + file_size * sizeof(jab_char));
    if (!data) {
        reportError("Memory allocation for input data failed");
        return 0;
    }
    fseek(fp, 0, SEEK_SET);
    if (fread(data->data, 1, file_size, fp) != file_size) {
        reportError("Reading input data file failed");
        free(data);
        fclose(fp);
        return 0;
    }
    fclose(fp);
    data->length = file_size;

    // --output logic
    filename = (jab_char *) imagePath;

    if (options != NULL) {
        // --color-number logic
        int tmp = getJavaIntField(env, options, "colorNumber", JAVA_TYPES.integer);
        if (tmp != 8) {
            color_number = tmp;
            if (color_number != 4) {
                reportError("Invalid color number. Supported color number includes 4 and 8.");
                return 0;
            }
        }
        // --module-size logic
        tmp = getJavaIntField(env, options, "moduleSize", JAVA_TYPES.integer);
        if (tmp != 12) {
            module_size = tmp;
            if (module_size < 0) {
                printf("Invalid moduleSize. Number must be non-negative.");
                return 0;
            }
        }
    }

    // check input
    if (!data) {
        reportError("Input data missing");
        return 0;
    } else if (data->length == 0) {
        reportError("Input data is empty");
        return 0;
    }
    if (!filename) {
        reportError("Output file missing");
        return 0;
    }
    if (symbol_number == 0) {
        symbol_number = 1;
    }

    // check input
    if (symbol_number == 1 && symbol_positions) {
        if (symbol_positions[0] != 0) {
            reportError("Incorrect symbol position value for master symbol.");
            return 0;
        }
    }
    if (symbol_number > 1 && symbol_positions_number != symbol_number) {
        reportError("Symbol position information is incomplete for multi-symbol code");
        return 0;
    }
    if (symbol_number > 1 && symbol_versions_number != symbol_number) {
        reportError("Symbol version information is incomplete for multi-symbol code");
        return 0;
    }
    return 1;
}

/**
 * @brief JABCode reader modified main function
 * @param env the JNIEnv instance using for allocating byte arrays
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
 * @brief JABCode writer modified main function
 * @param env the JNIEnv instance used for reading Java object fields
 * @param sourcePath the absolute path of the file containing data to be included in the code
 * @param imagePath the absolute path of the image to be analyzed
 * @param options the options object containing configuration
 * @param clearSource wether to delete the source file after successful generation
 * @return 1: success | 0: failure
 */
jab_int32 generate(JNIEnv *env, const char *sourcePath, const char *imagePath, jobject options,
                   jboolean clearSource) {
    if (!parseCommandLineParameters(env, sourcePath, imagePath, options)) {
        return 1;
    }

    // create encode parameter object
    jab_encode *enc = createEncode(color_number, symbol_number);
    if (enc == NULL) {
        cleanMemory();
        reportError("Creating encode parameter failed");
        return 1;
    }
    if (module_size > 0) {
        enc->module_size = module_size;
    }
    if (master_symbol_width > 0) {
        enc->master_symbol_width = master_symbol_width;
    }
    if (master_symbol_height > 0) {
        enc->master_symbol_height = master_symbol_height;
    }
    for (jab_int32 loop = 0; loop < symbol_number; loop++) {
        if (symbol_ecc_levels) enc->symbol_ecc_levels[loop] = symbol_ecc_levels[loop];
        if (symbol_versions) enc->symbol_versions[loop] = symbol_versions[loop];
        if (symbol_positions) enc->symbol_positions[loop] = symbol_positions[loop];
    }

    // generate JABCode
    if (generateJABCode(enc, data) != 0) {
        reportError("Creating jab code failed");
        destroyEncode(enc);
        cleanMemory();
        return 1;
    }

    // save bitmap in image file
    jab_int32 result = 0;
    if (color_space == 0) {
        if (!saveImage(enc->bitmap, filename)) {
            reportError("Saving png image failed");
            result = 1;
        }
    } else if (color_space == 1) {
        if (!saveImageCMYK(enc->bitmap, 0, filename)) {
            reportError("Saving tiff image failed");
            result = 1;
        }
    }

    destroyEncode(enc);
    cleanMemory();
    if (clearSource == JNI_TRUE) remove(sourcePath);
    return result;
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

/**
 * @brief JNICall to the Kotlin 'generate' function
 * @param env the JNIEnv instance
 * @param thiz the calling Java object (unused)
 * @param sourcePath the absolute path of the file containing data to be included in the code
 * @param imagePath the absolute path of the image to be analyzed
 * @param options the options object containing configuration
 * @param clearSource wether to delete the source file after successful generation
 * @return 1: success | 0: failure
 */
JNIEXPORT int JNICALL Java_de_cyb3rko_jabcodelib_JabCodeLib_generate(
    JNIEnv *env, __attribute__((unused)) jobject thiz, jstring sourcePath, jstring imagePath,
    jobject options, jboolean clearSource) {
    return generate(env, (*env)->GetStringUTFChars(env, sourcePath, JNI_FALSE),
                    (*env)->GetStringUTFChars(env, imagePath, JNI_FALSE), options, clearSource);
}
