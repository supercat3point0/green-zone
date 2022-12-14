import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.FloatBuffer;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.MemoryStack;
import org.joml.Matrix4f;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL32C.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import static net.catech.util.Resources.getResourceAsString;

public class Main {
  private static final GLFWErrorCallback errorCallback = GLFWErrorCallback.createPrint(System.err);
  private static long window, lastLoopTime;
  private static IntBuffer width, height;
  private static int vao, vbo;
  private static ProgramInfo programInfo;
  private static float rotation = 0f;

  private static final GLFWKeyCallback keyCallback = new GLFWKeyCallback() {
    @Override
    public void invoke(long window, int key, int scancode, int action, int mods) {
      if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) glfwSetWindowShouldClose(window, true);
    }
  };

  private static void init() {
    GLFWVidMode vidMode;
    int vert, frag;

    glfwSetErrorCallback(errorCallback);

    if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");

    glfwDefaultWindowHints();
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
    glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
    window = glfwCreateWindow(640, 480, "Hello, World!", NULL, NULL);
    if (window == NULL) {
      glfwTerminate();
      throw new RuntimeException("Failed to create the GLFW window");
    }

    vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
    if (vidMode == null) {
      glfwTerminate();
      throw new RuntimeException("Failed to fetch video mode");
    }
    glfwSetWindowPos(window, (vidMode.width() - 640) / 2, (vidMode.height() - 480) / 2);

    glfwMakeContextCurrent(window);
    GL.createCapabilities();

    glfwSwapInterval(1);

    glfwSetKeyCallback(window, keyCallback);

    width = MemoryUtil.memAllocInt(1);
    height = MemoryUtil.memAllocInt(1);

    vao = glGenVertexArrays();
    glBindVertexArray(vao);

    try (MemoryStack stack = MemoryStack.stackPush()) {
      FloatBuffer vertices = stack.mallocFloat(6 * 7);

      vertices.put(-0.5f).put(0.5f).put(0f).put(0f).put(0f).put(0f).put(0f)
              .put(-0.5f).put(-0.5f).put(0f).put(0f).put(0f).put(1f).put(0f)
              .put(0.5f).put(-0.5f).put(0f).put(0f).put(1f).put(1f).put(0f)
              .put(0.5f).put(-0.5f).put(0f).put(0f).put(1f).put(1f).put(0f)
              .put(0.5f).put(0.5f).put(0f).put(0f).put(1f).put(0f).put(0f)
              .put(-0.5f).put(0.5f).put(0f).put(0f).put(0f).put(0f).put(0f);
      vertices.flip();

      vbo = glGenBuffers();
      glBindBuffer(GL_ARRAY_BUFFER, vbo);
      glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
    }

    try {
      vert = glCreateShader(GL_VERTEX_SHADER);
      glShaderSource(vert, getResourceAsString("/res/shaders/default.vert"));
      glCompileShader(vert);
      if (glGetShaderi(vert, GL_COMPILE_STATUS) != GL_TRUE) throw new RuntimeException(glGetShaderInfoLog(vert));

      frag = glCreateShader(GL_FRAGMENT_SHADER);
      glShaderSource(frag, getResourceAsString("/res/shaders/default.frag"));
      glCompileShader(frag);
      if (glGetShaderi(frag, GL_COMPILE_STATUS) != GL_TRUE) throw new RuntimeException(glGetShaderInfoLog(frag));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    programInfo = new ProgramInfo();
    glAttachShader(programInfo.program, vert);
    glAttachShader(programInfo.program, frag);
    glBindFragDataLocation(programInfo.program, 0, "fragColor");
    glLinkProgram(programInfo.program);
    if (glGetProgrami(programInfo.program, GL_LINK_STATUS) != GL_TRUE) throw new RuntimeException(glGetProgramInfoLog(programInfo.program));
    glUseProgram(programInfo.program);

    programInfo.attributes.put("position", glGetAttribLocation(programInfo.program, "position"));
    glEnableVertexAttribArray(programInfo.attributes.get("position"));
    glVertexAttribPointer(programInfo.attributes.get("position"), 3, GL_FLOAT, false, 7 * 4, 0);

    programInfo.attributes.put("color", glGetAttribLocation(programInfo.program, "color"));
    glEnableVertexAttribArray(programInfo.attributes.get("color"));
    glVertexAttribPointer(programInfo.attributes.get("color"), 4, GL_FLOAT, false, 7 * 4, 3 * 4);

    lastLoopTime = System.nanoTime();
  }

  private static void input() {}

  private static void update(float delta) {
    rotation += delta * 50f * (float) Math.PI / 180f;
    rotation %= 2f * (float) Math.PI;
  }

  private static void draw() {
    float ratio;
    Matrix4f model, view, projection;

    glfwGetFramebufferSize(window, width, height);
    ratio = width.get() / (float) height.get();
    width.rewind();
    height.rewind();

    try (MemoryStack stack = MemoryStack.stackPush()) {
      FloatBuffer matrix = stack.mallocFloat(16);

      programInfo.uniforms.put("model", glGetUniformLocation(programInfo.program, "model"));
      model = new Matrix4f().rotate(rotation, 0f, 0f, 1f);
      glUniformMatrix4fv(programInfo.uniforms.get("model"), false, model.get(matrix));

      programInfo.uniforms.put("view", glGetUniformLocation(programInfo.program, "view"));
      view = new Matrix4f();
      glUniformMatrix4fv(programInfo.uniforms.get("view"), false, view.get(matrix));

      programInfo.uniforms.put("projection", glGetUniformLocation(programInfo.program, "projection"));
      projection = new Matrix4f().ortho(-ratio, ratio, -1f, 1f, -1f, 1f);
      glUniformMatrix4fv(programInfo.uniforms.get("projection"), false, projection.get(matrix));
    }

    glViewport(0, 0, width.get(), height.get());
    glClear(GL_COLOR_BUFFER_BIT);
    glDrawArrays(GL_TRIANGLES, 0, 6);

    glfwSwapBuffers(window);
    glfwPollEvents();

    width.flip();
    height.flip();
  }

  private static void dispose() {
    glDeleteVertexArrays(vao);
    glDeleteBuffers(vbo);

    MemoryUtil.memFree(width);
    MemoryUtil.memFree(height);

    glfwDestroyWindow(window);
    keyCallback.free();
    glfwTerminate();
    errorCallback.free();
  }

  public static void main(String[] args) {
    float accumulator = 0f, interval = 1f / 60f;

    init();

    while (!glfwWindowShouldClose(window)) {
      long time = System.nanoTime();
      float delta = (time - lastLoopTime) / 1000000000f;
      boolean render = false;

      lastLoopTime = time;
      accumulator += delta;
      if (accumulator >= 1f / 60f) render = true;

      input();

      while (accumulator >= interval) {
        update(interval);
        accumulator -= interval;
      }

      if (render) draw();
    }

    dispose();
  }
}
