import java.util.HashMap;

import static org.lwjgl.opengl.GL20C.glCreateProgram;

public class ProgramInfo {
  public int program = glCreateProgram();
  public HashMap<String, Integer> attributes;
  public HashMap<String, Integer> uniforms;

  ProgramInfo(int attribInitialCapacity, float attribLoadFactor, int uniformInitialCapacity, float uniformLoadFactor) {
    attributes = new HashMap<>(attribInitialCapacity, attribLoadFactor);
    uniforms = new HashMap<>(uniformInitialCapacity, uniformLoadFactor);
  }

  ProgramInfo(int attribInitialCapacity, int uniformInitialCapacity) {
    attributes = new HashMap<>(attribInitialCapacity);
    uniforms = new HashMap<>(uniformInitialCapacity);
  }

  ProgramInfo(int initialCapacity, float loadFactor) {
    attributes = new HashMap<>(initialCapacity, loadFactor);
    uniforms = new HashMap<>(initialCapacity, loadFactor);
  }

  ProgramInfo(int initialCapacity) {
    attributes = new HashMap<>(initialCapacity);
    uniforms = new HashMap<>(initialCapacity);
  }

  ProgramInfo() {
    attributes = new HashMap<>();
    uniforms = new HashMap<>();
  }
}
