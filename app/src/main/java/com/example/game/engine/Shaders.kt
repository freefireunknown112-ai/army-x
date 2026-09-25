package com.example.game.engine

import android.opengl.GLES20
import android.util.Log

object Shaders {
    const val VERTEX_SHADER = """
        uniform mat4 uMVPMatrix;
        uniform mat4 uModelMatrix;
        attribute vec4 aPosition;
        attribute vec4 aColor;
        attribute vec3 aNormal;
        
        varying vec4 vColor;
        varying vec3 vNormal;
        varying vec3 vFragPos;
        varying float vDistance;

        void main() {
            vec4 worldPos = uModelMatrix * aPosition;
            vFragPos = worldPos.xyz;
            vNormal = normalize((uModelMatrix * vec4(aNormal, 0.0)).xyz);
            vColor = aColor;
            gl_Position = uMVPMatrix * aPosition;
            vDistance = gl_Position.z;
        }
    """

    const val FRAGMENT_SHADER = """
        precision mediump float;
        varying vec4 vColor;
        varying vec3 vNormal;
        varying vec3 vFragPos;
        varying float vDistance;

        uniform vec3 uLightDir;
        uniform vec3 uLightColor;
        uniform vec3 uAmbientColor;
        uniform vec4 uFogColor;
        uniform float uFogStart;
        uniform float uFogEnd;

        void main() {
            vec3 norm = normalize(vNormal);
            float diff = max(dot(norm, -uLightDir), 0.0);
            vec3 diffuse = diff * uLightColor;
            vec3 lighting = uAmbientColor + diffuse;
            vec3 finalColor = vColor.rgb * lighting;

            // Fog computation
            float fogFactor = clamp((vDistance - uFogStart) / (uFogEnd - uFogStart), 0.0, 1.0);
            vec3 colorWithFog = mix(finalColor, uFogColor.rgb, fogFactor);

            gl_FragColor = vec4(colorWithFog, vColor.a);
        }
    """

    // Emissive shader for safe zone energy shield and bullet tracers
    const val EMISSIVE_VERTEX_SHADER = """
        uniform mat4 uMVPMatrix;
        attribute vec4 aPosition;
        attribute vec4 aColor;
        varying vec4 vColor;
        void main() {
            vColor = aColor;
            gl_Position = uMVPMatrix * aPosition;
        }
    """

    const val EMISSIVE_FRAGMENT_SHADER = """
        precision mediump float;
        varying vec4 vColor;
        uniform float uPulseAlpha;
        void main() {
            gl_FragColor = vec4(vColor.rgb, vColor.a * uPulseAlpha);
        }
    """

    fun compileShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] == 0) {
            Log.e("ARMY_X_SHADERS", "Shader compilation error: " + GLES20.glGetShaderInfoLog(shader))
            GLES20.glDeleteShader(shader)
            return 0
        }
        return shader
    }

    fun createProgram(vertexCode: String, fragmentCode: String): Int {
        val vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, vertexCode)
        val fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentCode)
        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)
        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            Log.e("ARMY_X_SHADERS", "Program link error: " + GLES20.glGetProgramInfoLog(program))
            GLES20.glDeleteProgram(program)
            return 0
        }
        return program
    }
}
