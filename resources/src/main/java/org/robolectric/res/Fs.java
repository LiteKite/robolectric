package org.robolectric.res;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.robolectric.util.Util;

@SuppressWarnings("NewApi")
abstract public class Fs {
  public static FileSystem forJar(URL url) {
    return forJar(Paths.get(toUri(url)));
  }

  public static FileSystem forJar(Path file) {
    try {
      return FileSystems.newFileSystem(file, null);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static Path fromURL(URL url) {
    switch (url.getProtocol()) {
      case "file":
        return Paths.get(toUri(url));
      case "jar":
        throw new UnsupportedOperationException();
        // String[] parts = url.getPath().split("!", 0);
        // try {
        //   Fs fs = fromJar(new URL(parts[0]));
        //   return fs.join(parts[1].substring(1));
        // } catch (MalformedURLException e) {
        //   throw new IllegalArgumentException(e);
        // }
      default:
        throw new IllegalArgumentException("unsupported fs type for '" + url + "'");
    }
  }

  public static URI toUri(URL url) {
    try {
      return url.toURI();
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("invalid URL: " + url, e);
    }
  }

  public static String baseNameFor(Path path) {
    String name = path.getFileName().toString();
    int dotIndex = name.indexOf(".");
    return dotIndex >= 0 ? name.substring(0, dotIndex) : name;
  }

  public static InputStream getInputStream(Path path) throws IOException {
    return Files.newInputStream(path);
  }

  public static byte[] getBytes(Path path) throws IOException {
    return Util.readBytes(getInputStream(path));
  }

  public static Path[] listFiles(Path path) throws IOException {
    try (Stream<Path> list = Files.list(path)) {
      return list.toArray(Path[]::new);
    }
  }

  public static Path[] listFiles(Path path, final Predicate<Path> filter) throws IOException {
    try (Stream<Path> list = Files.list(path)) {
      return list.filter(filter).toArray(Path[]::new);
    }
  }

  public static String[] listFileNames(Path path) {
    File[] files = path.toFile().listFiles();
    if (files == null) return null;
    String[] strings = new String[files.length];
    for (int i = 0; i < files.length; i++) {
      strings[i] = files[i].getName();
    }
    return strings;
  }

  public static Path join(Path path, String... pathParts) {
    for (String pathPart : pathParts) {
      path = path.resolve(pathPart);
    }
    return path;
  }
}
