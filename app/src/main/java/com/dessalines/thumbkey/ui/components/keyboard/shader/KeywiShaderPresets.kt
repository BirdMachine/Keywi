package com.dessalines.thumbkey.ui.components.keyboard.shader

/**
 * Built-in single-pass Shadertoy-style fragment shaders.
 * User code must define void mainImage(out vec4 fragColor, in vec2 fragCoord).
 */
object KeywiShaderPresets {
    const val MOLTEN = """
        void mainImage(out vec4 fragColor, in vec2 fragCoord) {
            vec2 uv = fragCoord / iResolution.xy;
            float t = iTime * 0.35;
            float waves = sin(uv.x * 17.0 + sin(uv.y * 9.0 - t) * 2.0 + t);
            float veins = pow(max(0.0, sin(uv.y * 20.0 - t * 1.2 + waves * 2.0)), 6.0);
            vec3 rock = mix(vec3(0.07, 0.01, 0.025), vec3(0.45, 0.035, 0.01), 0.5 + 0.5 * waves);
            vec3 lava = vec3(1.0, 0.19 + veins * 0.43, 0.02);
            fragColor = vec4(mix(rock, lava, veins), 1.0);
        }
    """

    /** Transparent overlay so that original key legends remain readable. */
    const val SHIMMER = """
        void mainImage(out vec4 fragColor, in vec2 fragCoord) {
            vec2 uv = fragCoord / iResolution.xy;
            float d = abs(uv.x - (0.5 + 0.48 * sin(iTime * 0.38)));
            float band = pow(max(0.0, 1.0 - d * 8.0), 7.0);
            vec3 tint = 0.5 + 0.5 * cos(vec3(0.0, 2.1, 4.2) + uv.y * 5.0 + iTime);
            fragColor = vec4(tint, band * 0.20);
        }
    """
}
