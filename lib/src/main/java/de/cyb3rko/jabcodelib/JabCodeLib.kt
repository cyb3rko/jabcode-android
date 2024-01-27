package de.cyb3rko.jabcodelib

class JabCodeLib {
    init {
        System.loadLibrary("jabcodelib")
    }

    external fun detect(): Int
}
