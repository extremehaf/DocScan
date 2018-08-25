package scan.lucas.com.docscan

import android.hardware.Camera
import java.lang.Long.signum
import java.util.*

/**
 * Compares two `Size`s based on their areas.
 */
internal class CompareCameraSizesByArea : Comparator<Camera.Size> {

    // We cast here to ensure the multiplications won't overflow
    override fun compare(lhs: Camera.Size, rhs: Camera.Size) =
            signum(lhs.width.toLong() * lhs.height - rhs.width.toLong() * rhs.height)

}
