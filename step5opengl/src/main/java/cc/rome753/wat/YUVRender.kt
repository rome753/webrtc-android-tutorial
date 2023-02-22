package cc.rome753.wat

import android.media.Image
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class YUVRender: GLSurfaceView.Renderer {

    var yuvShader: YUVShader? = null
//    var yuvShader = DiffShader()
    var imageBytes: ImageBytes? = null

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        if (yuvShader == null) {
            yuvShader = YUVShader()
            yuvShader!!.init()
            GLES20.glClearColor(0.5f, 0.5f, 0.5f, 0.5f)
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        imageBytes?.let {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
            yuvShader!!.draw(it)
        }
    }

}