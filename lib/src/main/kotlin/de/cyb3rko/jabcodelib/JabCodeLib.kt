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

package de.cyb3rko.jabcodelib

/**
 * Native interface between the native jabcode implementation and Kotlin/Java
 * applications.
 *
 * @author Niko Diamadis (niko@cyb3rko.de)
 */
class JabCodeLib {
    // Load the native library when creating an object of the class.
    init {
        System.loadLibrary("jabcodelib")
    }

    /**
     * Native interface between the jabcode implementation and Kotlin/Java
     * applications.
     *
     * @param imagePath the absolute path of the image to be analyzed
     * @return [ByteArray] containing raw data as found in the code, null if
     * nothing found inside the image.
     */
    external fun detect(imagePath: String): ByteArray?
}
