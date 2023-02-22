#version 300 es

precision mediump float;
in vec3 aColor;
in vec2 aTexCoord;
out vec4 fragColor;

uniform sampler2D tex0;
uniform sampler2D tex1;
uniform sampler2D tex2;

void main() {

     float y = texture(tex0, aTexCoord).r;
     float u = texture(tex1, aTexCoord).r - 0.5;
     float v = texture(tex1, aTexCoord).a - 0.5;

//     float y = texture(tex0, aTexCoord).r;
//     float u = texture(tex2, aTexCoord).r - 0.5;
//     float v = texture(tex1, aTexCoord).r - 0.5;

    vec3 yuv = vec3(y, v, u);

    // 下面两种视觉上差异不大

//     // BT.601, which is the standard for SDTV is provided as a reference
//      vec3 rgb = mat3(      1,       1,       1,
//      0, -.39465, 2.03211,
//      1.13983, -.58060,       0) * yuv;

     // Using BT.709 which is the standard for HDTV
     vec3 rgb = mat3(      1,       1,       1,
     0, -.21482, 2.12798,
     1.28033, -.38059,       0) * yuv;


     // fragColor = vec4(rgb, 1.0);
     fragColor = 1.0f - vec4(rgb, 1.0);

}