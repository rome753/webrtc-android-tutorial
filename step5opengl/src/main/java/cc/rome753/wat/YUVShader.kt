package cc.rome753.wat

import android.media.Image
import android.opengl.GLES20.*
import android.opengl.GLES30
import android.opengl.GLES30.glBindVertexArray
import android.opengl.GLES30.glGenVertexArrays
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer

class YUVShader {

    var vertices = floatArrayOf( //     ---- 位置 ----       ---- 颜色 ----     - 纹理坐标 -
        -1f, -1f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,  // 左下
        1f, -1f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f,  // 右下
        -1f, 1f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, // 左上
        1f, 1f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f  // 右上
    )

    val indices = intArrayOf( // 注意索引从0开始!
        0, 1, 2,  // 第一个三角形
        1, 2, 3 // 第二个三角形
    )

    var program = 0
    var vertexBuffer: FloatBuffer? = null
    var intBuffer: IntBuffer? = null
    var vao: IntArray = IntArray(1)

    var tex: IntArray = IntArray(3) // yuv

    var transform = FloatArray(16)

    fun init() {
        program = ShaderUtils.loadProgramYUV()
        //分配内存空间,每个浮点型占4字节空间
        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        //传入指定的坐标数据
        vertexBuffer!!.put(vertices)
        vertexBuffer!!.position(0)
        vao = IntArray(1)
        glGenVertexArrays(1, vao, 0)
        glBindVertexArray(vao[0])
        val vbo = IntArray(1)
        glGenBuffers(1, vbo, 0)
        glBindBuffer(GL_ARRAY_BUFFER, vbo[0])
        glBufferData(GL_ARRAY_BUFFER, vertices.size * 4, vertexBuffer, GL_STATIC_DRAW)

        intBuffer = IntBuffer.allocate(indices.size * 4)
        intBuffer!!.put(indices)
        intBuffer!!.position(0)
        val ebo = IntArray(1)
        glGenBuffers(1, ebo, 0)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo[0])
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices.size * 4, intBuffer, GL_STATIC_DRAW)

        glUseProgram(program)
        glGenTextures(3, tex, 0)
        for (i in 0..2) {
            glActiveTexture(GL_TEXTURE0 + i)
            glBindTexture(GL_TEXTURE_2D, tex[i])
            // 为当前绑定的纹理对象设置环绕、过滤方式
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
//            val bitmap: Bitmap = ShaderUtils.loadImageAssets("face.png")
//            GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0)
//            glGenerateMipmap(GL_TEXTURE_2D)

            val loc0 = glGetUniformLocation(program, "tex$i")
            glUniform1i(loc0, i)
        }

        // Load the vertex data
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 8 * 4, 0)
        glEnableVertexAttribArray(0)
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 8 * 4, 3 * 4)
        glEnableVertexAttribArray(1)
        glVertexAttribPointer(2, 2, GL_FLOAT, false, 8 * 4, 6 * 4)
        glEnableVertexAttribArray(2)

        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindVertexArray(0)

        Matrix.setIdentityM(transform, 0)

    }

    fun draw(ib: ImageBytes) {
        draw(ib.width, ib.height, ib.bufY, ib.bufU, ib.bufV)
    }

    fun draw(w0: Int, h0: Int, bufY: ByteBuffer, bufU: ByteBuffer, bufV: ByteBuffer) {
        val w1 = w0 / 2
        val h1 = h0 / 2
        glUseProgram(program)

        glActiveTexture(tex[0])
        glBindTexture(GL_TEXTURE_2D, tex[0])
        glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, w0, h0, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE, bufY)

        glActiveTexture(tex[1])
        glBindTexture(GL_TEXTURE_2D, tex[1])
        glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE_ALPHA, w1, h1, 0, GL_LUMINANCE_ALPHA, GL_UNSIGNED_BYTE, bufU)
//        glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, w1, h1, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE, ib.bufU)

        glActiveTexture(tex[2])
        glBindTexture(GL_TEXTURE_2D, tex[2])
        glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE_ALPHA, w1, h1, 0, GL_LUMINANCE_ALPHA, GL_UNSIGNED_BYTE, bufV)
//        glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, w1, h1, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE, ib.bufV)

        val loc = glGetUniformLocation(program, "transform")
        glUniformMatrix4fv(loc, 1, false, transform, 0)

        glBindVertexArray(vao[0])
        glDrawElements(GL_TRIANGLES, vertices.size, GL_UNSIGNED_INT, 0)
    }

}