import java.nio.IntBuffer;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Main {
  private static final GLFWErrorCallback errorCallback = GLFWErrorCallback.createPrint(System.err);

  private static final GLFWKeyCallback keyCallback = new GLFWKeyCallback() {
    @Override
    public void invoke(long window, int key, int scancode, int action, int mods) {
      if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) glfwSetWindowShouldClose(window, true);
    }
  };

  public static void main(String[] args) {
    long window;
    GLFWVidMode vidMode;
    IntBuffer width, height;

    glfwSetErrorCallback(errorCallback);

    if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");

    window = glfwCreateWindow(640, 480, "Hello, World!", NULL, NULL);
    if (window == NULL) {
      glfwTerminate();
      throw new RuntimeException("Failed to create the GLFW window");
    }

    vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
    assert vidMode != null : "Could not get video mode";
    glfwSetWindowPos(window, (vidMode.width() - 640) / 2, (vidMode.height() - 480) / 2);

    glfwMakeContextCurrent(window);
    GL.createCapabilities();

    glfwSwapInterval(1);

    glfwSetKeyCallback(window, keyCallback);

    width = MemoryUtil.memAllocInt(1);
    height = MemoryUtil.memAllocInt(1);

    while (!glfwWindowShouldClose(window)) {
      float ratio;

      glfwGetFramebufferSize(window, width, height);
      ratio = width.get() / (float) height.get();

      width.rewind();
      height.rewind();

      glViewport(0, 0, width.get(), height.get());
      glClear(GL_COLOR_BUFFER_BIT);

      glMatrixMode(GL_PROJECTION);
      glLoadIdentity();
      glOrtho(-ratio, ratio, -1f, 1f, 1f, -1f);
      glMatrixMode(GL_MODELVIEW);

      glLoadIdentity();
      glRotatef((float) glfwGetTime() * 50f, 0f, 0f, 1f);

      glBegin(GL_TRIANGLES);
      glColor3f(0f, 0f, 0f);
      glVertex3f(-0.5f, 0.5f, 0f);
      glColor3f(0f, 0f, 1f);
      glVertex3f(-0.5f, -0.5f, 0f);
      glColor3f(0f, 1f, 1f);
      glVertex3f(0.5f, -0.5f, 0f);
      glColor3f(0f, 1f, 1f);
      glVertex3f(0.5f, -0.5f, 0f);
      glColor3f(0f, 1f, 0f);
      glVertex3f(0.5f, 0.5f, 0f);
      glColor3f(0f, 0f, 0f);
      glVertex3f(-0.5f, 0.5f, 0f);
      glEnd();

      glfwSwapBuffers(window);
      glfwPollEvents();

      width.flip();
      height.flip();
    }

    MemoryUtil.memFree(width);
    MemoryUtil.memFree(height);

    glfwDestroyWindow(window);
    keyCallback.free();
    glfwTerminate();
    errorCallback.free();
  }
}
