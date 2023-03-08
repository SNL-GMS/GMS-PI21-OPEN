package gms.shared.frameworks.osd.coi.util;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestUtilitiesTests {

  public static String testFactory1(String testString, Integer testInt) {
    Objects.requireNonNull(testString, "Testing for non null testString");
    Objects.requireNonNull(testInt, "Testing for non null testInt");

    return testString + testInt;
  }

  public static String testFactory2(String testString, Integer testInt) {
    Objects.requireNonNull(testString, "Testing for non null testString");
    return testString + testInt;
  }

  public static String testFactory3(String testString, Number testNum) {
    Objects.requireNonNull(testString, "Testing for non null testString");
    Objects.requireNonNull(testNum, "Testing for non null testNum");
    return testString + testNum;
  }

  public static String testFactory4(String testString, int testInt) {
    Objects.requireNonNull(testString, "Testing for non null testString");
    return testString + testInt;
  }

  public static String testFactory5(String testString, int testInt) {
    throw new NullPointerException();
  }

  @Test
  public void testCheckConstructorValidatesNullArguments() {
    TestUtilities.checkConstructorValidatesNullArguments(Test1.class, 1);
  }

  @Test
  public void testCheckConstructorValidatesNullArgumentsExpectException() {
    RuntimeException exception = assertThrows(RuntimeException.class,
      () -> TestUtilities.checkConstructorValidatesNullArguments(Test2.class, 1));
    assertEquals("No validation for argument of type " + Integer.class
        + " at parameter index 0 for constructor"
        + " public gms.shared.frameworks.osd.coi.util.TestUtilitiesTests$Test2(java.lang.Integer)",
      exception.getMessage());
  }

  @Test
  void testCheckConstructorValidatesNullArgumentsSubclassArgument() {
    TestUtilities.checkConstructorValidatesNullArguments(Test3.class, 1);
  }

  @Test
  public void testCheckConstructorValidatesNullArgumentsPrimitiveArgument() {
    TestUtilities.checkConstructorValidatesNullArguments(Test4.class, 1);
  }

  @Test
  public void testCheckConstructorValidatesNullArgumentsNoConstructorFound() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
      () -> TestUtilities.checkConstructorValidatesNullArguments(Test1.class, "Test"));
    assertTrue(exception.getMessage()
      .contains("No constructor signature found for arguments: [" + String.class + "]"));
  }

  @Test
  void testCheckAllConstructorsValidateNullArguments() {
    TestUtilities
      .checkAllConstructorsValidateNullArguments(Test5.class, new Object[][]{{"Test"}, {1}});
  }

  @Test
  public void testCheckAllConstructorsValidateNullArgumentsWhenValidArgsThrowsException() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
      () -> TestUtilities.checkConstructorValidatesNullArguments(Test6.class, 1));
    assertTrue(exception.getMessage()
      .contains("Could not call constructor with supposedly valid arguments"));
  }

  @Test
  public void testCheckAllConstructorsValidateNullArgumentsMissingParameters() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
      () -> TestUtilities
        .checkAllConstructorsValidateNullArguments(Test5.class, new Object[][]{{"Test"}}));
    assertEquals("Missing validation arguments for following constructors: "
        + "public gms.shared.frameworks.osd.coi.util.TestUtilitiesTests"
        + "$Test5(java.lang.Integer)",
      exception.getMessage());

  }


  @Test
  public void testCheckStaticMethodValidatesNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(TestUtilitiesTests.class,
      "testFactory1", "TestString", 1);
  }

  @Test
  void testCheckStaticMethodValidateNullArgumentsWhenValidArgsThrowsException()
    throws Exception {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
      () -> TestUtilities.checkStaticMethodValidatesNullArguments(TestUtilitiesTests.class,
        "testFactory5", "TestString", 1));
    assertTrue(exception.getMessage()
      .contains("Could not call static method with supposedly valid arguments"));
  }

  @Test
  void testCheckStaticMethodValidatesNullArgumentsExpectException() throws Exception {
    RuntimeException exception = assertThrows(RuntimeException.class,
      () -> TestUtilities.checkStaticMethodValidatesNullArguments(TestUtilitiesTests.class,
        "testFactory2", "TestString", 1));
    assertTrue(exception.getMessage().contains("No validation for argument of type " + Integer.class
      + " at parameter index 1 for method testFactory2"));
  }

  @Test
  void testCheckStaticMethodValidatesNullArgumentsNullAllowed() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullableArguments(TestUtilitiesTests.class,
      "testFactory2", Collections.singletonList(1), "TestString", 1);
  }

  @Test
  void testCheckStaticMethodValidatesNullArgumentsExpectsSuperclassPassesSubclass()
    throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(TestUtilitiesTests.class,
      "testFactory3", "TestString", 1);
  }

  @Test
  public void testCheckStaticMethodValidatesNullArgumentsPrimitiveArgument() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(TestUtilitiesTests.class,
      "testFactory4", "TestString", 1);
  }

  public static class Test1 {

    public Test1(Integer testInt) {
      Objects.requireNonNull(testInt, "Testing for non null testInt");
    }
  }

  public static class Test2 {

    public Test2(Integer testInt) {

    }
  }

  public static class Test3 {

    public Test3(Number testNum) {
      Objects.requireNonNull(testNum, "Testing for non null testNum");
    }
  }

  public static class Test4 {

    public Test4(int testInt) {
    }
  }

  public static class Test5 {

    public Test5(String testString) {
      Objects.requireNonNull(testString, "Testing for non null testString");
    }

    public Test5(Integer testInt) {
      Objects.requireNonNull(testInt, "Testing for non null testInt");
    }
  }

  public static class Test6 {

    public Test6(Integer testInt) {
      throw new NullPointerException();
    }
  }
}
