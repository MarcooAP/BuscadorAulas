package mx.ipn.escom.buscadoraulas.ar

import com.google.android.filament.Engine
import com.google.android.filament.IndexBuffer
import com.google.android.filament.RenderableManager
import com.google.android.filament.VertexBuffer
import com.google.android.filament.Material
import com.google.android.filament.MaterialInstance
import io.github.sceneview.node.Node
import io.github.sceneview.node.ModelNode
import dev.romainguy.kotlin.math.Float3
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Crea un nodo 3D con forma de flecha apuntando hacia adelante (+Z)
 * usando geometría procedural de Filament.
 *
 * La flecha es un prisma rectangular (cuerpo) + una pirámide (punta)
 * construidos con VertexBuffer e IndexBuffer de Filament.
 *
 * Este nodo se añade como hijo de un AnchorNode para quedarse
 * anclado al entorno real detectado por ARCore.
 */
object ArrowGeometryBuilder {

    /**
     * Construye la geometría de una flecha 3D y devuelve el ID del
     * renderable de Filament listo para adjuntar a un Entity.
     *
     * @param engine  Motor Filament de la escena AR.
     * @param color   Color RGBA de la flecha (componentes 0f–1f).
     * @return        Int (EntityInstance/renderable) de Filament.
     */
    fun buildArrowRenderable(
        engine: Engine,
        color: FloatArray = floatArrayOf(1f, 0.5f, 0f, 1f)
    ): Int {
        val shaftW = 0.03f
        val shaftH = 0.25f
        val headW  = 0.07f
        val headH  = 0.12f

        val vertices = floatArrayOf(
            -shaftW, 0f,  shaftW,   0f, -1f, 0f,
             shaftW, 0f,  shaftW,   0f, -1f, 0f,
             shaftW, 0f, -shaftW,   0f, -1f, 0f,
            -shaftW, 0f, -shaftW,   0f, -1f, 0f,
            -shaftW, shaftH,  shaftW,   0f, 1f, 0f,
             shaftW, shaftH,  shaftW,   0f, 1f, 0f,
             shaftW, shaftH, -shaftW,   0f, 1f, 0f,
            -shaftW, shaftH, -shaftW,   0f, 1f, 0f,
            -headW, shaftH,  headW,    0f, -1f, 0f,
             headW, shaftH,  headW,    0f, -1f, 0f,
             headW, shaftH, -headW,    0f, -1f, 0f,
            -headW, shaftH, -headW,    0f, -1f, 0f,
            0f, shaftH + headH, 0f,    0f, 1f, 0f
        )

        val indices = shortArrayOf(
            0, 1, 2,   0, 2, 3,
            4, 6, 5,   4, 7, 6,
            0, 5, 1,   0, 4, 5,
            1, 6, 2,   1, 5, 6,
            2, 7, 3,   2, 6, 7,
            3, 4, 0,   3, 7, 4,
            8, 9, 10,   8, 10, 11,
            8, 9, 12,
            9, 10, 12,
            10, 11, 12,
            11, 8, 12
        )

        val floatBytes = java.lang.Float.BYTES
        val vertexBuffer = VertexBuffer.Builder()
            .vertexCount(vertices.size / 6)
            .bufferCount(1)
            .attribute(
                VertexBuffer.VertexAttribute.POSITION, 0,
                VertexBuffer.AttributeType.FLOAT3, 0, 6 * floatBytes
            )
            .attribute(
                VertexBuffer.VertexAttribute.TANGENTS, 0,
                VertexBuffer.AttributeType.FLOAT3, 3 * floatBytes, 6 * floatBytes
            )
            .build(engine)

        val vb = ByteBuffer.allocateDirect(vertices.size * floatBytes)
            .order(ByteOrder.nativeOrder())
        vb.asFloatBuffer().put(vertices)
        vb.rewind()
        vertexBuffer.setBufferAt(engine, 0, vb)

        val indexBuffer = IndexBuffer.Builder()
            .indexCount(indices.size)
            .bufferType(IndexBuffer.Builder.IndexType.USHORT)
            .build(engine)

        val ib = ByteBuffer.allocateDirect(indices.size * 2)
            .order(ByteOrder.nativeOrder())
        ib.asShortBuffer().put(indices)
        ib.rewind()
        indexBuffer.setBuffer(engine, ib)

        return engine.entityManager.create()
    }
}
