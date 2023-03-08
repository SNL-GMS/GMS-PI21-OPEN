package gms.shared.frameworks.utilities;

import gms.shared.frameworks.common.ContentType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Provides utility methods extracting class and method information useful for automatically
 * generating service wrappers and their corresponding client proxies.
 */
public class ServiceReflectionUtilities {

  private ServiceReflectionUtilities() {
  }

  private static final Logger logger = LoggerFactory.getLogger(ServiceReflectionUtilities.class);

  /**
   * Obtains {@link PathMethod}s for each of the {@link Path} annotated operations in the provided
   * class. Validates the PathMethods have the correct signature (public, one input parameter,
   * return a value, also annotated with {@link POST}).  Validates the PathMethods all have unique
   * paths.  {@link PathMethod#getRelativePath()} on the returned PathMethods includes the base path
   * from the class-level {@link Path} annotation.
   *
   * @param cls class containing {@link Path} annotated handler operations, not null
   * @return Set of {@link PathMethod} containing the {@link Path} annotated handler operations of
   * the class, not null
   * @throws IllegalArgumentException if any of the {@link Path} operations have incorrect
   * signatures; if any of the {@link Path} operations have replicated path strings; if the
   * class-level {@link Path} annotation has an incorrect path
   * @throws NullPointerException if class is null
   */
  public static Set<PathMethod> findPathAnnotatedMethods(Class<?> cls) {

    Objects.requireNonNull(cls, "findPathAnnotatedMethods requires non-null class");

    // Find the methods annotated with @Path
    final Set<PathMethod> pathMethods = findPathMethods(cls);

    // Verify the @Path methods have valid relative paths
    verifyRelativePaths(pathMethods);

    // Verify each path is unique
    verifyUniqueRelativePaths(pathMethods);

    // Verify the @Path methods have the correct signatures
    verifyMethodSignatures(pathMethods);

    // Verify the @Consumes and @Produces ContentTypes match the method input and return parameters
    verifyContentTypes(pathMethods);

    return pathMethods;
  }

  /**
   * Obtains {@link PathMethod}s for the {@link Path} annotated operations in the provided class,
   * but throws an exception if any of the methods in the class is not a PathMethod (doesn't have
   * annotations) or isn't abstract. Validates the PathMethods have the correct signature (public,
   * one input parameter, method is annotated with {@link POST}).  Validates the PathMethods all
   * have unique paths.  {@link PathMethod#getRelativePath()} on the returned PathMethods includes
   * the base path from the class-level {@link Path} annotation.
   *
   * @param cls class containing {@link Path} annotated handler operations, not null
   * @return Set of {@link PathMethod} containing the {@link Path} annotated handler operations of
   * the class, not null
   * @throws IllegalArgumentException if there are any operations in the class that are not abstract
   * and/or do not have proper annotations; if any of the {@link Path} operations have incorrect
   * signatures; if any of the {@link Path} operations have replicated path strings; if the
   * class-level {@link Path} annotation has an incorrect path
   * @throws NullPointerException if class is null
   */
  public static Set<PathMethod> findPathAnnotatedMethodsOnlyOrThrow(Class<?> cls) {
    final Set<PathMethod> pathMethods = findPathAnnotatedMethods(cls);
    final Set<Method> declaredMethods = getMethods(cls);
    throwIfNotSameMethods(declaredMethods, onlyAbstract(declaredMethods),
      "These methods need to be abstract (implies not default)");
    throwIfNotSameMethods(declaredMethods, methods(pathMethods),
      "These methods are not PathMethods (needs annotations and proper signature)");
    return pathMethods;
  }

  /**
   * Determines if the given method returns void.
   *
   * @param m the method
   * @return true if the given method returns void, false otherwise
   */
  public static boolean methodReturnsVoid(Method m) {
    Objects.requireNonNull(m, "null method");
    return m.getReturnType().equals(Void.TYPE);
  }

  /**
   * Returns on the methods that are abstract from the provided methods.
   *
   * @param methods the methods to filter
   * @return the subset of the given methods that are abstract
   */
  private static Set<Method> onlyAbstract(Set<Method> methods) {
    return methods.stream().filter(m -> Modifier.isAbstract(m.getModifiers()))
      .collect(Collectors.toSet());
  }

  /**
   * Retrieves all Method's from the given PathMethod's
   *
   * @param pathMethods the PathMethod's
   * @return all of the Method's from the PathMethod's
   */
  private static Set<Method> methods(Set<PathMethod> pathMethods) {
    return pathMethods.stream().map(PathMethod::getMethod).collect(Collectors.toSet());
  }

  /**
   * Throws an exception if the two sets of Method's are not the same, formatting the exceptions
   * nicely.  The set difference is part of the exception message.
   *
   * @param s1 the first set of methods
   * @param s2 the second set of methods
   * @param msg a message to put before the set differences
   * @throws IllegalArgumentException if the two sets are not equal
   */
  private static void throwIfNotSameMethods(Set<Method> s1, Set<Method> s2, String msg) {
    if (!s1.equals(s2)) {
      final Set<Method> diff = difference(s1, s2);
      final Set<String> methodStrs = diff.stream()
        .map(Method::toGenericString).collect(Collectors.toSet());
      throw new IllegalArgumentException(msg + " : " + methodStrs);
    }
  }

  /**
   * Computes set difference.
   *
   * @param s1 the first set
   * @param s2 the second set
   * @param <T> the type of elements in the set
   * @return a set that contains the elements that are in s1 but not in s2
   */
  private static <T> Set<T> difference(Set<T> s1, Set<T> s2) {
    final Set<T> diff = new HashSet<>(s1);
    diff.removeAll(s2);
    return diff;
  }

  /**
   * Finds the methods in cls which have an {@link Path} annotation and constructs a {@link
   * PathMethod} for each of those methods.  The PathMethods include the base path from the
   * class-level {@link Path} annotation. Only base path validation occurs in this operation.
   *
   * @param cls class containing {@link Path} annotated handler operations, not null
   * @return Set of {@link PathMethod} containing the classes {@link Path} annotated handler
   * operations, not null
   */
  private static Set<PathMethod> findPathMethods(Class<?> cls) {
    return findRelativePathsByMethod(cls).entrySet().stream()
      .map(e -> createPathMethod(e.getValue(), e.getKey()))
      .collect(Collectors.toSet());
  }

  /**
   * Finds the methods in cls which have an {@link Path} annotation and constructs a {@link Method}
   * to String map for each of those methods.  The strings are the relative path for the method
   * created by combining the class-level {@link Path} annotation value with the Path annotation
   * value from each Method.
   *
   * @param cls class containing {@link Path} annotated handler operations, not null
   * @return Map of Method to path String for each {@link Path} annotated operation, not null
   */
  private static Map<Method, String> findRelativePathsByMethod(Class<?> cls) {

    // Find the base path used by all of the @Path methods
    final String basePath = getBasePath(cls);

    // Find Methods annotated with @Path and collect them in a (Method -> relativePath) String map
    final Map<Method, String> potentialRouteHandlerMethods = getMethods(cls)
      .stream().collect(Collectors.toMap(Function.identity(),
        m -> AnnotationUtils.findMethodAnnotation(m, Path.class)))
      .entrySet().stream()
      .filter(e -> e.getValue().isPresent())
      .collect(Collectors.toMap(
        Entry::getKey, e -> createOperationPath(basePath, e.getValue().get().value())));

    logger.trace("Found {} potential route handler methods", potentialRouteHandlerMethods.size());
    potentialRouteHandlerMethods.keySet()
      .forEach(m -> logger.trace("Potential route handler: {}", m));

    return potentialRouteHandlerMethods;
  }

  private static Set<Method> getMethods(Class<?> cls) {
    return new HashSet<>(Arrays.asList(cls.getMethods()));
  }

  /**
   * Obtains the base path from the class-level ({@link Path} annotation.  The returned base has a
   * trailing '/'
   *
   * @param cls get the value form the Path annotation on this class, not null
   * @return String containing the base url route, not null.
   * @throws IllegalArgumentException if the Path annotation is missing; if the Path annotation's
   * value is not a valid path string
   */
  private static String getBasePath(Class<?> cls) {
    final String message = "Classes with @Path annotated operations must also have an @Path "
      + "annotation on the class definition with value providing the base path for all routes "
      + "exposed by that class.";

    final String basePath = correctPathOnClassFormatting(
      AnnotationUtils.findClassAnnotation(cls, Path.class)
        .map(Path::value)
        .orElseThrow(() -> new IllegalArgumentException(message)));
    if (!isValidPath(basePath)) {
      throw new IllegalArgumentException(
        "Service's @Path defines an invalid base path of '" + basePath + "'");
    }

    return basePath;
  }

  /**
   * Obtain the full path to an operation by combing the basePath with the operation's relative
   * path.  Assumes the provided basePath but not the relative path has the standard path formatting
   * (see {@link ServiceReflectionUtilities#correctPathOnMethodFormatting(String)}.
   *
   * @param basePath base path to the operation, not null
   * @param relativePath operation's path relative to the base path, not null
   * @return String containing a correctly formatted path to the operation, not null
   */
  private static String createOperationPath(String basePath, String relativePath) {
    return basePath + correctPathOnMethodFormatting(relativePath);
  }

  /**
   * Corrects the provided path from @Path on a method: trim whitespace, remove leading and trailing
   * '/' if present.
   *
   * @param path String containing a relative path, not null
   * @return String containing the corrected path
   */
  private static String correctPathOnMethodFormatting(String path) {
    return removeLeadingSlashesIfPresent
      .andThen(removeTrailingSlashesIfPresent).apply(path);
  }

  /**
   * Corrects the provided path from @Path on a class: add tailing '/' if not present.
   *
   * @param path String containing a relative path, not null
   * @return String containing the corrected path
   */
  private static String correctPathOnClassFormatting(String path) {
    return addTrailSlashIfNotPresent.apply(path);
  }

  private static final UnaryOperator<String> addTrailSlashIfNotPresent
    = s -> StringUtils.appendIfMissing(s, "/");

  private static final UnaryOperator<String> removeTrailingSlashesIfPresent
    = s -> StringUtils.stripEnd(s, "/");

  private static final UnaryOperator<String> removeLeadingSlashesIfPresent
    = s -> StringUtils.stripStart(s, "/");

  /**
   * Obtain a {@link PathMethod} containing information from the provided {@link Method} which has
   * the provided relativePath
   *
   * @param relativePath method's expose path (already includes the base path), not null
   * @param method {@link Method} which is exposed at relativePath
   * @return {@link PathMethod} corresponding to the provided method and relativePath, not null
   */
  private static PathMethod createPathMethod(String relativePath, Method method) {
    final var consumes = determineConsumesContentTypeFromAnnotation(method);
    final var produces = determineProducesContentTypeFromAnnotation(method);

    return PathMethod.from(relativePath, method, consumes, produces);
  }

  /**
   * Determines the {@link ContentType} consumed by the provided {@link Method}. The ContentType is
   * either declared by an @{@link Consumes} annotation or a default.
   *
   * @param method find the {@link ContentType} consumed by this Method, not null
   * @return {@link ContentType}, not null
   * @throws IllegalArgumentException if the Method has an @{@link Consumes} annotation but a
   * ContentType can't be parsed from the annotation's value.
   */
  private static ContentType determineConsumesContentTypeFromAnnotation(Method method) {
    return determineContentTypeFromAnnotation(
      method, Consumes.class, Consumes::value, ContentType.defaultContentType());
  }

  /**
   * Determines the {@link ContentType} produced by the provided {@link Method}. The ContentType is
   * either declared by an @{@link Produces} annotation or a default.
   *
   * @param method find the {@link ContentType} produced by this Method, not null
   * @return {@link ContentType}, not null
   * @throws IllegalArgumentException if the Method has an @{@link Produces} annotation but a
   * ContentType can't be parsed from the annotation's value.
   */
  private static ContentType determineProducesContentTypeFromAnnotation(Method method) {
    return determineContentTypeFromAnnotation(
      method, Produces.class, Produces::value, determineDefaultProducesContentType(method));
  }

  /**
   * Determines the default {@link ContentType} produced by the provided {@link Method}. The Method
   * will produce a streaming ContentType if it returns a {@link Flux} and a non-streaming
   * ContentType otherwise.
   *
   * @param method find the appropriate default ContentType produced by this {@link Method}, not
   * null
   * @return {@link ContentType}, not null
   */
  private static ContentType determineDefaultProducesContentType(Method method) {
    if (hasStreamingReturnParameterType(method)) {
      return ContentType.defaultStreamingContentType();
    }

    return ContentType.defaultContentType();
  }

  /**
   * Find a {@link ContentType} for the provided {@link Method}. The ContentType will either be
   * extracted and parsed from the declared annotation (using annClass and annValuesExtractor) or be
   * the provided defaultContentType if the method is not annotated.
   *
   * @param method find the ContentType for this {@link Method}, not null
   * @param annClass {@link Annotation} class declaring the ContentType, not null
   * @param annValuesExtractor function extracting the content type string from the Annotation, not
   * null
   * @param defaultContentType default {@link ContentType} used when the method is not annotated
   * @param <T> Annotation type
   * @return {@link ContentType} (extracted from the Annotation or the defaultContentType), not null
   * @throws IllegalArgumentException if the Method has an "annClass" annotation but a ContentType
   * can't be parsed from the value extracted from the annotation.
   */
  private static <T extends Annotation> ContentType determineContentTypeFromAnnotation(
    Method method, Class<T> annClass, Function<T, String[]> annValuesExtractor,
    ContentType defaultContentType) {

    final Function<T, ContentType> extractContentTypeClosure =
      extractContentType(method, annClass, annValuesExtractor);

    return AnnotationUtils.findMethodAnnotation(method, annClass)
      .map(extractContentTypeClosure)
      .orElse(defaultContentType);
  }

  private static <T extends Annotation> Function<T, ContentType> extractContentType(Method method,
    Class<T> annotationClass, Function<T, String[]> annValuesExtractor) {

    return annotation -> {
      final String[] annotationValues = annValuesExtractor.apply(annotation);
      Validate.isTrue(annotationValues.length == 1,
        "Exactly one content type allowed in annotation " + annotationClass.getSimpleName()
          + "; offending method: " + method.toGenericString());
      return ContentType.parse(annotationValues[0]);
    };
  }

  /**
   * Verifies the {@link PathMethod}s have valid relative paths.  Raises an exception listing any
   * PathMethods with invalid {@link PathMethod#getRelativePath()}.  {@link
   * ServiceReflectionUtilities#isValidPath(String)} determines path validity.
   *
   * @param pathMethods Collection of {@link PathMethod}s to verify, not null
   * @throws IllegalArgumentException if any of the pathMethods have invalid paths
   */
  private static void verifyRelativePaths(Collection<PathMethod> pathMethods) {
    final List<Method> methodsWithInvalidPaths = pathMethods.stream()
      .filter(e -> !isValidPath(e.getRelativePath()))
      .map(PathMethod::getMethod)
      .collect(Collectors.toList());

    if (!methodsWithInvalidPaths.isEmpty()) {
      final String description = "@Path operations must have values containing valid relative "
        + "paths. The following operations have invalid paths:\n";

      Validation.throwForMethods(description, methodsWithInvalidPaths);
    }
  }

  /**
   * Validates the provided path String is a valid path according to {@link URI#URI(String)}
   *
   * @param path String containing the path to validate, not null
   * @return whether the path is valid
   */
  private static boolean isValidPath(String path) {
    try {
      new URI(path);
      return !StringUtils.containsWhitespace(path);
    } catch (URISyntaxException e) {
      logger.debug("Invalid path: {}", path, e);
      return false;
    }
  }

  /**
   * Verifies each of the {@link PathMethod}s has a unique relative path and raises an exception
   * listing any repeated paths.
   *
   * @param pathMethods Collection of {@link PathMethod}s that must be unique, not null
   * @throws IllegalArgumentException if any entries in pathMethods have replicated {@link
   * PathMethod#getRelativePath()}
   */
  private static void verifyUniqueRelativePaths(Collection<PathMethod> pathMethods) {
    final String description = "A Service cannot have more than one endpoint operation with "
      + "the same relative path. These relative paths are repeated:";

    Validation.throwForNonUnique(pathMethods, PathMethod::getRelativePath, description);
  }

  /**
   * Verifies the {@link PathMethod}s have valid signatures.  Raises an exception listing any
   * PathMethods with invalid {@link PathMethod#getRelativePath()}.  A PathMethod has a valid
   * signature if it is:
   * <p>
   * 1. Has public visibility
   * <p>
   * 2. Accepts a single input parameter
   * <p>
   * 3. Is also annotated with {@link POST}
   *
   * @param pathMethods Collection of {@link PathMethod}s to verify, not null
   * @throws IllegalArgumentException if any of the pathMethods have invalid signatures
   */
  private static void verifyMethodSignatures(Collection<PathMethod> pathMethods) {
    final Predicate<Method> routeHandlerTest = method -> Optional.of(method)
      .filter(m -> Modifier.isPublic(m.getModifiers()))
      .filter(m -> m.getParameterCount() == 1)
      .filter(m -> AnnotationUtils.findMethodAnnotation(m, POST.class).isPresent())
      .isPresent();

    final List<Method> methodsWithInvalidSignatures = pathMethods.stream()
      .map(PathMethod::getMethod)
      .filter(m -> routeHandlerTest.negate().test(m))
      .collect(Collectors.toList());

    logger.trace("Found {} invalid route handler methods", methodsWithInvalidSignatures.size());
    methodsWithInvalidSignatures.forEach(m -> logger.trace("Invalid route handler: {}", m));

    // Raise an IllegalArgumentException if any @Path method has an incorrect signature
    final String description = "@Path operations must be public, accept one input parameter, "
      + "and also have an @POST annotation. These @Path operations have "
      + "incorrect signatures:\n";

    throwForDescriptionAndMethods(description, methodsWithInvalidSignatures);
  }

  /**
   * Verifies the {@link PathMethod}s have content types matching their input and output parameters.
   * Raises an exception listing any PathMethods with invalid {@link PathMethod#getInputFormat()} or
   * {@link PathMethod#getOutputFormat()}. A PathMethod has valid content types if it's:
   * <p>
   * 1. Input format is a non-streaming type
   * <p>
   * 2. Output format is a streaming type only when the operation returns a {@link Flux}
   *
   * @param pathMethods Collection of {@link PathMethod}s to verify, not null
   * @throws IllegalArgumentException if any of the pathMethods have invalid content types
   */
  private static void verifyContentTypes(Collection<PathMethod> pathMethods) {
    verifyMethodsConsumeNonStreamingInputs(pathMethods);
    verifyMethodsReturningFluxProduceStreamingContentType(pathMethods);
    verifyMethodsReturningObjectProduceNonStreamingContentType(pathMethods);
  }

  /**
   * Verifies none of the provided {@link PathMethod}s @Consume a streaming ContentType.
   *
   * @param pathMethods Collection of {@link PathMethod}s to verify, not null
   * @throws IllegalArgumentException if any of the pathMethods have a streaming input ContentType.
   */
  private static void verifyMethodsConsumeNonStreamingInputs(Collection<PathMethod> pathMethods) {

    final List<PathMethod> invalidMethods = pathMethods.stream()
      .filter(m -> ContentType.isStreaming(m.getInputFormat()))
      .collect(Collectors.toList());

    logger.trace("Found {} methods with streaming request types", invalidMethods.size());
    invalidMethods.forEach(m -> logger.trace("Invalid streaming request type: {}", m));

    // Raise an IllegalArgumentException if any @Path method has an incorrect request type
    final String description = "Operations cannot @Consume streaming ContentTypes."
      + " These @Path annotated operations have invalid request types:\n";

    throwForDescriptionAndPathMethods(description, invalidMethods);
  }

  /**
   * Verifies any of the provided {@link PathMethod}s which have a streaming return parameter type
   * also produce a streaming ContentType.
   *
   * @param pathMethods Collection of {@link PathMethod}s to verify, not null
   * @throws IllegalArgumentException if any of the pathMethods have a streaming return parameter
   * type but a non-streaming ContentType.
   */
  private static void verifyMethodsReturningFluxProduceStreamingContentType(
    Collection<PathMethod> pathMethods) {

    final List<PathMethod> invalidMethods = pathMethods.stream()
      .filter(m -> hasStreamingReturnParameterType(m.getMethod()))
      .filter(m -> !ContentType.isStreaming(m.getOutputFormat()))
      .collect(Collectors.toList());

    logger
      .trace("Found {} methods with invalid non-streaming response types", invalidMethods.size());
    invalidMethods.forEach(m -> logger.trace("Invalid non-streaming response type: {}", m));

    // Raise an IllegalArgumentException if any @Path method has an incorrect response type
    final String description = "Operations producing a streaming response must declare a "
      + "streaming response ContentType in the @Produces annotation or omit the @Produces "
      + "annotation to use the default streaming ContentType. These @Path annotated operations "
      + "have inconsistent response types:\n";

    throwForDescriptionAndPathMethods(description, invalidMethods);
  }

  /**
   * Verifies any of the provided {@link PathMethod}s which have a normal, non-streaming return
   * parameter type produce a non-streaming Content-Type.
   *
   * @param pathMethods Collection of {@link PathMethod}s to verify, not null
   * @throws IllegalArgumentException if any of the pathMethods have a non-streaming return
   * parameter type but a streaming ContentType.
   */
  private static void verifyMethodsReturningObjectProduceNonStreamingContentType(
    Collection<PathMethod> pathMethods) {

    final List<PathMethod> invalidMethods = pathMethods.stream()
      .filter(m -> !hasStreamingReturnParameterType(m.getMethod()))
      .filter(m -> ContentType.isStreaming(m.getOutputFormat()))
      .collect(Collectors.toList());

    logger.trace("Found {} methods with invalid streaming response types", invalidMethods.size());
    invalidMethods.forEach(m -> logger.trace("Invalid streaming response type: {}", m));

    // Raise an IllegalArgumentException if any @Path method has an incorrect response type
    final String description = "Operations producing a single response must "
      + "declare a non-streaming response ContentType in the @Produces annotation or omit the "
      + "@Produces annotation to use the default ContentType. These @Path annotated operations "
      + "have inconsistent response types:\n";

    throwForDescriptionAndPathMethods(description, invalidMethods);
  }

  /**
   * If the pathMethods collection is not empty, throws an {@link IllegalArgumentException} with a
   * message containing the provided description and method names.
   *
   * @param description exception description message, not null
   * @param pathMethods {@link PathMethod}s leading to the exception, potentially empty, not null
   * @throws IllegalArgumentException when the provided pathMethods collection is not empty
   */
  private static void throwForDescriptionAndPathMethods(String description,
    Collection<PathMethod> pathMethods) {
    throwForDescriptionAndMethods(description,
      pathMethods.stream().map(PathMethod::getMethod).collect(Collectors.toList()));
  }

  /**
   * If the methods collection is not empty, throws an {@link IllegalArgumentException} with a
   * message containing the provided description and method names.
   *
   * @param description exception description message, not null
   * @param methods {@link Method}s leading to the exception, potentially empty, not null
   * @throws IllegalArgumentException when the provided methods collection is not empty
   */
  private static void throwForDescriptionAndMethods(String description,
    Collection<Method> methods) {
    if (!methods.isEmpty()) {
      Validation.throwForMethods(description, methods);
    }
  }

  /**
   * Determines whether the provided {@link Method} has a {@link Method#getReturnType()}
   * corresponding to a route with a streaming response.s
   *
   * @param method {@link Method}, not null
   * @return boolean indicating whether the Method produces a streaming response
   */
  private static boolean hasStreamingReturnParameterType(Method method) {
    return Flux.class.isAssignableFrom(method.getReturnType());
  }

  public static String getContextRoot(Class<?> cls) {
    Objects.requireNonNull(cls, "getContextRoot requires non-null class");
    String pathValue = AnnotationUtils.findClassAnnotation(cls, Path.class)
      .orElseThrow(() -> new IllegalArgumentException("Client interface must have @Path"))
      .value();


    String[] pathValArray = pathValue.split("/");
    return Arrays.stream(pathValArray)
      .skip(1)
      .findFirst()
      .orElseThrow(() -> new IllegalArgumentException("Client interface must have have a path defined with /path"));
  }
}
