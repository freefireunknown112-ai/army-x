package com.example.game.engine

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.example.data.models.ItemCategory
import com.example.game.GameSession
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin

class GameRenderer3D(
    private val context: Context,
    val session: GameSession
) : GLSurfaceView.Renderer {

    private var programMain: Int = 0
    private var programEmissive: Int = 0

    // Matrices
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    // Camera
    var cameraPitch: Float = 15f
    var cameraYaw: Float = 0f
    private var cameraDist: Float = 4.2f
    private val cameraTarget = Vector3()
    private val cameraEye = Vector3()

    // 3D Meshes
    private lateinit var cubeMesh: Geometry3D.Mesh
    private lateinit var terrainMesh: Geometry3D.Mesh
    private lateinit var zoneCylinderMesh: Geometry3D.Mesh

    // Screen dimensions
    private var viewWidth: Int = 1920
    private var viewHeight: Int = 1080

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.09f, 0.11f, 0.14f, 1.0f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthFunc(GLES20.GL_LEQUAL)
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        GLES20.glCullFace(GLES20.GL_BACK)

        programMain = Shaders.createProgram(Shaders.VERTEX_SHADER, Shaders.FRAGMENT_SHADER)
        programEmissive = Shaders.createProgram(Shaders.EMISSIVE_VERTEX_SHADER, Shaders.EMISSIVE_FRAGMENT_SHADER)

        cubeMesh = Geometry3D.buildCubeMesh(0.5f, 0.5f, 0.5f)
        terrainMesh = Geometry3D.buildTerrainMesh(gridSize = 40, totalSize = 240f)
        zoneCylinderMesh = Geometry3D.buildCylinderMesh(segments = 48, radius = 1.0f, height = 25f, r = 0.8f, g = 0.2f, b = 0.9f, a = 0.35f)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        viewWidth = width
        viewHeight = height
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // Setup Perspective
        val aspectRatio = viewWidth.toFloat() / viewHeight.coerceAtLeast(1).toFloat()
        val fov = if (session.player.isAimingDownSights) 40f else 65f
        Matrix.perspectiveM(projectionMatrix, 0, fov, aspectRatio, 0.5f, 350f)

        // Update Camera Position based on player & input
        val p = session.player
        cameraYaw = p.yaw
        cameraPitch = p.pitch.coerceIn(-35f, 55f)
        val targetDist = if (p.isAimingDownSights) 2.2f else 4.2f
        cameraDist += (targetDist - cameraDist) * 0.2f

        val pitchRad = Math.toRadians(cameraPitch.toDouble())
        val yawRad = Math.toRadians(cameraYaw.toDouble())

        // Camera shoulder offset: slightly to the right for tactical third-person view
        val shoulderOffset = if (p.isAimingDownSights) 0.55f else 0.45f
        val shoulderX = cos(yawRad).toFloat() * shoulderOffset
        val shoulderZ = sin(yawRad).toFloat() * shoulderOffset

        val eyeY = p.position.y + 1.6f + sin(pitchRad).toFloat() * cameraDist
        val eyeH = cos(pitchRad).toFloat() * cameraDist
        val eyeX = p.position.x - sin(yawRad).toFloat() * eyeH + shoulderX
        val eyeZ = p.position.z + cos(yawRad).toFloat() * eyeH + shoulderZ

        cameraEye.set(eyeX, eyeY.coerceAtLeast(0.6f), eyeZ)
        cameraTarget.set(p.position.x + shoulderX, p.position.y + 1.4f, p.position.z + shoulderZ)

        Matrix.setLookAtM(
            viewMatrix, 0,
            cameraEye.x, cameraEye.y, cameraEye.z,
            cameraTarget.x, cameraTarget.y, cameraTarget.z,
            0f, 1f, 0f
        )

        // Render Lit World (Terrain, Structures, Props, Entities)
        GLES20.glUseProgram(programMain)
        setupLightingAndFog(programMain)

        // 1. Terrain
        Matrix.setIdentityM(modelMatrix, 0)
        drawMesh(programMain, terrainMesh, modelMatrix)

        // 2. Map Structures
        for (s in session.map.structures) {
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, s.position.x, s.position.y, s.position.z)
            Matrix.scaleM(modelMatrix, 0, s.size.x, s.size.y, s.size.z)
            drawTintedCube(programMain, modelMatrix, s.colorR, s.colorG, s.colorB)
        }

        // 3. Cover Crates
        for (c in session.map.coverProps) {
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, c.position.x, c.position.y, c.position.z)
            Matrix.scaleM(modelMatrix, 0, c.size.x, c.size.y, c.size.z)
            drawTintedCube(programMain, modelMatrix, c.colorR, c.colorG, c.colorB)
        }

        // 4. World Loot
        for (loot in session.map.initialLoot) {
            if (!loot.isSpawned) continue
            Matrix.setIdentityM(modelMatrix, 0)
            val hoverY = loot.y + (sin(System.currentTimeMillis() * 0.004).toFloat() * 0.15f)
            Matrix.translateM(modelMatrix, 0, loot.x, hoverY, loot.z)
            val rot = (System.currentTimeMillis() % 3600) / 10f
            Matrix.rotateM(modelMatrix, 0, rot, 0f, 1f, 0f)
            Matrix.scaleM(modelMatrix, 0, 0.7f, 0.7f, 0.7f)

            val (r, g, b) = when (loot.item.category) {
                ItemCategory.WEAPON -> Triple(1.0f, 0.6f, 0.0f) // Gold/Orange
                ItemCategory.ARMOR -> Triple(0.2f, 0.6f, 1.0f) // Blue
                ItemCategory.MEDICAL -> Triple(0.1f, 0.9f, 0.4f) // Green
                ItemCategory.GRENADE -> Triple(0.9f, 0.2f, 0.2f) // Red
                else -> Triple(0.8f, 0.8f, 0.8f)
            }
            drawTintedCube(programMain, modelMatrix, r, g, b)
        }

        // 5. Vehicles
        for (v in session.map.vehicles) {
            renderVehicle(v)
        }

        // 6. Bots
        for (bot in session.bots) {
            if (bot.isAlive) {
                renderHumanoid(bot.position, bot.yaw, isEnemy = true)
            }
        }

        // 7. Player Character
        if (session.player.isAlive && !session.player.isDriving) {
            renderHumanoid(session.player.position, session.player.yaw, isEnemy = false)
        }

        // 8. Safe Zone Barrier (Emissive Transparency)
        GLES20.glDisable(GLES20.GL_CULL_FACE)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE)

        GLES20.glUseProgram(programEmissive)
        renderSafeZone()

        // 9. Bullet Tracers
        renderBulletTracers()

        GLES20.glDisable(GLES20.GL_BLEND)
        GLES20.glEnable(GLES20.GL_CULL_FACE)
    }

    private fun setupLightingAndFog(program: Int) {
        val lightDirLoc = GLES20.glGetUniformLocation(program, "uLightDir")
        val lightColorLoc = GLES20.glGetUniformLocation(program, "uLightColor")
        val ambientColorLoc = GLES20.glGetUniformLocation(program, "uAmbientColor")
        val fogColorLoc = GLES20.glGetUniformLocation(program, "uFogColor")
        val fogStartLoc = GLES20.glGetUniformLocation(program, "uFogStart")
        val fogEndLoc = GLES20.glGetUniformLocation(program, "uFogEnd")

        GLES20.glUniform3f(lightDirLoc, -0.6f, -0.8f, -0.4f)
        GLES20.glUniform3f(lightColorLoc, 0.95f, 0.92f, 0.85f)
        GLES20.glUniform3f(ambientColorLoc, 0.28f, 0.32f, 0.38f)
        GLES20.glUniform4f(fogColorLoc, 0.09f, 0.11f, 0.14f, 1.0f)
        GLES20.glUniform1f(fogStartLoc, 50f)
        GLES20.glUniform1f(fogEndLoc, 160f)
    }

    private fun renderHumanoid(pos: Vector3, yawDeg: Float, isEnemy: Boolean) {
        val (suitR, suitG, suitB) = if (isEnemy) Triple(0.72f, 0.22f, 0.22f) else Triple(0.22f, 0.42f, 0.28f)

        // Body / Torso
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, pos.x, pos.y + 0.9f, pos.z)
        Matrix.rotateM(modelMatrix, 0, yawDeg, 0f, 1f, 0f)
        Matrix.scaleM(modelMatrix, 0, 0.6f, 0.75f, 0.35f)
        drawTintedCube(programMain, modelMatrix, suitR, suitG, suitB)

        // Tactical Helmet / Head
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, pos.x, pos.y + 1.55f, pos.z)
        Matrix.rotateM(modelMatrix, 0, yawDeg, 0f, 1f, 0f)
        Matrix.scaleM(modelMatrix, 0, 0.38f, 0.38f, 0.38f)
        drawTintedCube(programMain, modelMatrix, 0.18f, 0.20f, 0.22f)

        // Visor glow
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, pos.x, pos.y + 1.55f, pos.z)
        Matrix.rotateM(modelMatrix, 0, yawDeg, 0f, 1f, 0f)
        Matrix.translateM(modelMatrix, 0, 0f, 0f, 0.2f)
        Matrix.scaleM(modelMatrix, 0, 0.26f, 0.10f, 0.05f)
        val visorColor = if (isEnemy) Triple(1.0f, 0.1f, 0.1f) else Triple(0.0f, 0.9f, 1.0f)
        drawTintedCube(programMain, modelMatrix, visorColor.first, visorColor.second, visorColor.third)

        // Weapon in hand
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, pos.x, pos.y + 0.95f, pos.z)
        Matrix.rotateM(modelMatrix, 0, yawDeg, 0f, 1f, 0f)
        Matrix.translateM(modelMatrix, 0, 0.35f, 0.0f, 0.45f)
        Matrix.scaleM(modelMatrix, 0, 0.12f, 0.15f, 0.65f)
        drawTintedCube(programMain, modelMatrix, 0.12f, 0.12f, 0.14f)

        // Left & Right Legs
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, pos.x, pos.y + 0.35f, pos.z)
        Matrix.rotateM(modelMatrix, 0, yawDeg, 0f, 1f, 0f)
        Matrix.translateM(modelMatrix, 0, -0.16f, 0f, 0f)
        Matrix.scaleM(modelMatrix, 0, 0.22f, 0.7f, 0.24f)
        drawTintedCube(programMain, modelMatrix, 0.16f, 0.18f, 0.20f)

        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, pos.x, pos.y + 0.35f, pos.z)
        Matrix.rotateM(modelMatrix, 0, yawDeg, 0f, 1f, 0f)
        Matrix.translateM(modelMatrix, 0, 0.16f, 0f, 0f)
        Matrix.scaleM(modelMatrix, 0, 0.22f, 0.7f, 0.24f)
        drawTintedCube(programMain, modelMatrix, 0.16f, 0.18f, 0.20f)
    }

    private fun renderVehicle(v: com.example.data.models.Vehicle) {
        // Chassis
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, v.x, v.y + 0.5f, v.z)
        Matrix.rotateM(modelMatrix, 0, v.rotationY, 0f, 1f, 0f)
        Matrix.scaleM(modelMatrix, 0, 2.2f, 0.8f, 4.0f)
        drawTintedCube(programMain, modelMatrix, 0.45f, 0.40f, 0.25f) // Desert Military Camo

        // Cabin Roll Cage
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, v.x, v.y + 1.2f, v.z)
        Matrix.rotateM(modelMatrix, 0, v.rotationY, 0f, 1f, 0f)
        Matrix.scaleM(modelMatrix, 0, 1.8f, 0.9f, 2.0f)
        drawTintedCube(programMain, modelMatrix, 0.15f, 0.15f, 0.15f)

        // 4 Wheels
        val wheelOffsets = listOf(
            Pair(-1.2f, -1.3f), Pair(1.2f, -1.3f),
            Pair(-1.2f, 1.3f), Pair(1.2f, 1.3f)
        )
        for (w in wheelOffsets) {
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, v.x, v.y + 0.35f, v.z)
            Matrix.rotateM(modelMatrix, 0, v.rotationY, 0f, 1f, 0f)
            Matrix.translateM(modelMatrix, 0, w.first, 0f, w.second)
            Matrix.scaleM(modelMatrix, 0, 0.4f, 0.7f, 0.7f)
            drawTintedCube(programMain, modelMatrix, 0.1f, 0.1f, 0.1f)
        }
    }

    private fun renderSafeZone() {
        val zone = session.safeZone
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, zone.currentCenter.x, 12f, zone.currentCenter.z)
        Matrix.scaleM(modelMatrix, 0, zone.currentRadius, 1f, zone.currentRadius)

        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)

        val mvpLoc = GLES20.glGetUniformLocation(programEmissive, "uMVPMatrix")
        val alphaLoc = GLES20.glGetUniformLocation(programEmissive, "uPulseAlpha")
        val pulse = 0.5f + sin(System.currentTimeMillis() * 0.005).toFloat() * 0.25f

        GLES20.glUniformMatrix4fv(mvpLoc, 1, false, mvpMatrix, 0)
        GLES20.glUniform1f(alphaLoc, pulse)

        val posLoc = GLES20.glGetAttribLocation(programEmissive, "aPosition")
        val colorLoc = GLES20.glGetAttribLocation(programEmissive, "aColor")

        GLES20.glEnableVertexAttribArray(posLoc)
        GLES20.glVertexAttribPointer(posLoc, 3, GLES20.GL_FLOAT, false, 0, zoneCylinderMesh.vertexBuffer)

        GLES20.glEnableVertexAttribArray(colorLoc)
        GLES20.glVertexAttribPointer(colorLoc, 4, GLES20.GL_FLOAT, false, 0, zoneCylinderMesh.colorBuffer)

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, zoneCylinderMesh.indexCount, GLES20.GL_UNSIGNED_SHORT, zoneCylinderMesh.indexBuffer)

        GLES20.glDisableVertexAttribArray(posLoc)
        GLES20.glDisableVertexAttribArray(colorLoc)
    }

    private fun renderBulletTracers() {
        val mvpLoc = GLES20.glGetUniformLocation(programEmissive, "uMVPMatrix")
        val alphaLoc = GLES20.glGetUniformLocation(programEmissive, "uPulseAlpha")
        GLES20.glUniform1f(alphaLoc, 1.0f)

        for (b in session.bulletTracers) {
            if (!b.isActive) continue
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, b.currentPos.x, b.currentPos.y, b.currentPos.z)
            Matrix.scaleM(modelMatrix, 0, 0.12f, 0.12f, 0.6f)

            Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)
            GLES20.glUniformMatrix4fv(mvpLoc, 1, false, mvpMatrix, 0)

            drawMeshDirect(programEmissive, cubeMesh)
        }
    }

    private fun drawTintedCube(program: Int, model: FloatArray, r: Float, g: Float, b: Float) {
        val mvpLoc = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        val modelLoc = GLES20.glGetUniformLocation(program, "uModelMatrix")

        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, model, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)

        GLES20.glUniformMatrix4fv(mvpLoc, 1, false, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(modelLoc, 1, false, model, 0)

        // Re-tint color buffer
        val colors = FloatArray(24 * 4)
        for (i in 0 until 24) {
            colors[i * 4 + 0] = r
            colors[i * 4 + 1] = g
            colors[i * 4 + 2] = b
            colors[i * 4 + 3] = 1f
        }
        val tempColorBuf = Geometry3D.createFloatBuffer(colors)

        val posLoc = GLES20.glGetAttribLocation(program, "aPosition")
        val normLoc = GLES20.glGetAttribLocation(program, "aNormal")
        val colorLoc = GLES20.glGetAttribLocation(program, "aColor")

        GLES20.glEnableVertexAttribArray(posLoc)
        GLES20.glVertexAttribPointer(posLoc, 3, GLES20.GL_FLOAT, false, 0, cubeMesh.vertexBuffer)

        GLES20.glEnableVertexAttribArray(normLoc)
        GLES20.glVertexAttribPointer(normLoc, 3, GLES20.GL_FLOAT, false, 0, cubeMesh.normalBuffer)

        GLES20.glEnableVertexAttribArray(colorLoc)
        GLES20.glVertexAttribPointer(colorLoc, 4, GLES20.GL_FLOAT, false, 0, tempColorBuf)

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, cubeMesh.indexCount, GLES20.GL_UNSIGNED_SHORT, cubeMesh.indexBuffer)

        GLES20.glDisableVertexAttribArray(posLoc)
        GLES20.glDisableVertexAttribArray(normLoc)
        GLES20.glDisableVertexAttribArray(colorLoc)
    }

    private fun drawMesh(program: Int, mesh: Geometry3D.Mesh, model: FloatArray) {
        val mvpLoc = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        val modelLoc = GLES20.glGetUniformLocation(program, "uModelMatrix")

        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, model, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)

        GLES20.glUniformMatrix4fv(mvpLoc, 1, false, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(modelLoc, 1, false, model, 0)

        val posLoc = GLES20.glGetAttribLocation(program, "aPosition")
        val normLoc = GLES20.glGetAttribLocation(program, "aNormal")
        val colorLoc = GLES20.glGetAttribLocation(program, "aColor")

        GLES20.glEnableVertexAttribArray(posLoc)
        GLES20.glVertexAttribPointer(posLoc, 3, GLES20.GL_FLOAT, false, 0, mesh.vertexBuffer)

        GLES20.glEnableVertexAttribArray(normLoc)
        GLES20.glVertexAttribPointer(normLoc, 3, GLES20.GL_FLOAT, false, 0, mesh.normalBuffer)

        GLES20.glEnableVertexAttribArray(colorLoc)
        GLES20.glVertexAttribPointer(colorLoc, 4, GLES20.GL_FLOAT, false, 0, mesh.colorBuffer)

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mesh.indexCount, GLES20.GL_UNSIGNED_SHORT, mesh.indexBuffer)

        GLES20.glDisableVertexAttribArray(posLoc)
        GLES20.glDisableVertexAttribArray(normLoc)
        GLES20.glDisableVertexAttribArray(colorLoc)
    }

    private fun drawMeshDirect(program: Int, mesh: Geometry3D.Mesh) {
        val posLoc = GLES20.glGetAttribLocation(program, "aPosition")
        val colorLoc = GLES20.glGetAttribLocation(program, "aColor")

        GLES20.glEnableVertexAttribArray(posLoc)
        GLES20.glVertexAttribPointer(posLoc, 3, GLES20.GL_FLOAT, false, 0, mesh.vertexBuffer)

        GLES20.glEnableVertexAttribArray(colorLoc)
        GLES20.glVertexAttribPointer(colorLoc, 4, GLES20.GL_FLOAT, false, 0, mesh.colorBuffer)

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mesh.indexCount, GLES20.GL_UNSIGNED_SHORT, mesh.indexBuffer)

        GLES20.glDisableVertexAttribArray(posLoc)
        GLES20.glDisableVertexAttribArray(colorLoc)
    }
}
