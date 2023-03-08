package gms.shared.frameworks.utilities;

import gms.shared.frameworks.common.ContentType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PathMethodTests {

  private static final Method method = PathMethodTests.class.getMethods()[0];

  @ParameterizedTest
  @ValueSource(strings = {"/pathString", "pathString"})
  void testFrom(String path) {
    final PathMethod pathMethod = PathMethod.from(path, method,
      ContentType.defaultContentType(), ContentType.MSGPACK);

    assertNotNull(pathMethod);
    assertAll(
      // whether the given path did or did not have a leading slash, PathMethod should add a leading slash
      () -> assertEquals("/pathString", pathMethod.getRelativePath()),
      () -> assertEquals(method, pathMethod.getMethod()),
      () -> assertEquals(ContentType.defaultContentType(), pathMethod.getInputFormat()),
      () -> assertEquals(ContentType.MSGPACK, pathMethod.getOutputFormat())
    );
  }
}
