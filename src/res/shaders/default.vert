#version 150 core

in vec3 position;
in vec4 color;

out vec4 vertColor;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

void main(void) {
  vertColor = color;
  gl_Position = projection * view * model * vec4(position, 1.0);
}
