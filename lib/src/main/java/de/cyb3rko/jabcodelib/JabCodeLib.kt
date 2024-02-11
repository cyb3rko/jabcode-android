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

class JabCodeLib {
    init {
        System.loadLibrary("jabcodelib")
    }

    external fun detect(imagePath: String): ByteArray?
}
