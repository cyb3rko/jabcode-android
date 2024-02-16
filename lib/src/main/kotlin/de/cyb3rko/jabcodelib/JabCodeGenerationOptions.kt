package de.cyb3rko.jabcodelib

/**
 * Configuration for jabcode generation.
 * Missing parameters are filled with default values.
 *
 * @param colorNumber Number of colors (4,8,default:8)
 * @param moduleSize Module size in pixel (default:12 pixels)
 * @author Niko Diamadis (niko@cyb3rko.de)
 */
 class JabCodeGenerationOptions(
    val colorNumber: Int = 8,
    val moduleSize: Int = 12
) {
    init {
        if (colorNumber != 4 && colorNumber != 8) {
            throw IllegalArgumentException("colorNumber has to be 4 or 8")
        }
        if (moduleSize < 0) {
            throw IllegalArgumentException("moduleSize must not be negative")
        }
    }
}
