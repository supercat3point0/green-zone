package net.catech.util;

import java.io.*;

public class Resources {
  public static BufferedReader getResource(String path) throws IOException {
    InputStream in = Resources.class.getResourceAsStream(path);

    if (in == null) throw new FileNotFoundException();
    return new BufferedReader(new InputStreamReader(in));
  }

  public static String getResourceAsString(String path) throws IOException {
    try (BufferedReader reader = getResource(path)) {
      StringBuilder builder = new StringBuilder();
      String line;

      while ((line = reader.readLine()) != null) {
        builder.append(line);
        builder.append('\n');
      }

      return builder.toString();
    }
  }
}
