package mx.ipn.escom.buscadoraulas

import com.google.android.filament.Colors
import io.github.sceneview.node.ModelNode
import org.junit.Test

class ColorTest {
    @Test
    fun test() {
        val type = Colors.RgbaType.SRGB
        val m: com.google.android.filament.gltfio.FilamentInstance? = null
        val mat = m?.materialInstances?.firstOrNull()
        mat?.setParameter("baseColorFactor", type, 0.0f, 0.5f, 1.0f, 1.0f)
    }
}
