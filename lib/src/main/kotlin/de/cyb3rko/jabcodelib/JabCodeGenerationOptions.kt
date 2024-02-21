package de.cyb3rko.jabcodelib

/**
 * Configuration for jabcode generation.
 * Missing parameters are filled with default values.
 *
 * @param colorNumber Number of colors (4,8, default:8)
 * @param moduleSize Module size in pixel (default:12 pixels)
 * @param symbolWidth Master symbol width in pixel
 * @param symbolHeight Master symbol height in pixel
 * @param symbolNumber Number of symbols (1-61, default:1)
 * @param symbolVersion Side-Version of each symbol, starting from master and
 * then slave symbols (x0 y0 x1 y1 x2 y2...)
 * @param symbolPosition Symbol positions (0-60), starting from master and then
 * slave symbols (p0 p1 p2...). Only required for multi-symbol code
 * @param eccLevel Error correction levels (1-10, default:3). If different for
 * each symbol, starting from master and then slave symbols (ecc0 ecc1 ecc2...).
 * For master symbol, level 0 means using the default level, for slaves, it
 * means using the same level as its host
 * @param colorSpace Color space of output image (0:RGB,1:CMYK,default:0). RGB
 * image is saved as PNG and CMYK image as TIFF
 * @author Niko Diamadis (niko@cyb3rko.de)
 */
class JabCodeGenerationOptions(
    private val colorNumber: Int = 8,
    private val moduleSize: Int = 12,
    private val symbolWidth: Int = 0,
    private val symbolHeight: Int = 0,
    private val symbolNumber: Int = 1,
    private val eccLevel: IntArray = intArrayOf(),
    private val symbolVersion: IntArray = intArrayOf(),
    private val symbolPosition: IntArray = intArrayOf(),
    private val colorSpace: Int = 0
) {
    init {
        if (colorNumber != 4 && colorNumber != 8) {
            throw IllegalArgumentException("colorNumber has to be 4 or 8")
        }
        if (moduleSize < 0) {
            throw IllegalArgumentException("moduleSize must not be negative")
        }
        if (symbolWidth < 0) {
            throw IllegalArgumentException("symbolWidth must not be negative")
        }
        if (symbolHeight < 0) {
            throw IllegalArgumentException("symbolHeight must not be negative")
        }
        if (symbolNumber !in 1..61) {
            throw IllegalArgumentException("symbolNumber must be in range 1-61")
        }
        if (symbolVersion.size % 2 != 0) {
            throw IllegalArgumentException("symbolVersion must be coordinate pairs")
        }
        symbolVersion.forEach {
            if (it !in 1..32) {
                throw IllegalArgumentException("symbolVersion coordinates must be in range 1 - 32")
            }
        }
        symbolPosition.forEach {
            if (it !in 0..60) {
                throw IllegalArgumentException("symbolPosition must be in range 0 - 60")
            }
        }
        eccLevel.forEach {
            if (it !in 0..10) {
                throw IllegalArgumentException("eccLevels must be in range 0 - 10")
            }
        }
        if (colorSpace !in 0..1) {
            throw IllegalArgumentException("colorSpace must be 0 or 1")
        }
    }
}
