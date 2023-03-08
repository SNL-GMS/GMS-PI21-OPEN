package gms.shared.frameworks.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.frameworks.common.HttpStatus.Code;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RouteGeneratorTests {

  @Test
  void testGenerateValidatesParameters() {
    assertEquals("RouteGenerator requires non-null businessObject",
      assertThrows(NullPointerException.class, () -> RouteGenerator.generate(null)).getMessage());
  }

  /**
   * Tests a generated route invokes the annotated route handler operation.  Uses a non-trivial
   * input and output class to test serialization.
   */
  @Test
  void testGeneratedRouteInvokesHandlerOperation() throws JsonProcessingException {

    class Handler implements HandlerInterface {

      public Compound<String> foo(Collection<Compound<String>> compounds) {
        final Compound<String> first = compounds.iterator().next();
        final Collection<String> newGenerics = first.getGenericCollection().stream()
          .map(s -> s + " changed")
          .collect(Collectors.toList());
        return Compound.create(first.getInt(), newGenerics);
      }
    }

    final Handler handlerSpy = spy(new Handler());

    // Verify there is a generated route for each @Path operation
    final Set<Route> generatedRoutes = RouteGenerator.generate(handlerSpy);
    final Set<String> expectedPaths = Set.of(BASE_PATH + INT_PATH, BASE_PATH + OBJECT_PATH,
      BASE_PATH + VOID_PATH, BASE_PATH + NULL_PATH, BASE_PATH + FLUX_PATH);
    assertAll(
      () -> assertNotNull(generatedRoutes),
      () -> assertEquals(expectedPaths.size(), generatedRoutes.size()),
      () -> assertEquals(expectedPaths,
        generatedRoutes.stream().map(Route::getPath).collect(Collectors.toSet())),
      () -> assertTrue(generatedRoutes.stream().map(Route::getHandler).noneMatch(Objects::isNull))
    );

    // Verify one of the routes has a RequestHandler delegating to the expected operation
    final RequestHandler<Compound> requestHandler = findHandlerOrThrow(generatedRoutes,
      OBJECT_PATH);
    final Collection<Compound<String>> input = List.of(Compound.create(10, List.of("hi")));
    final Compound<String> expectedOutput = Compound.create(10, List.of("hi changed"));

    final Response<Compound> response = requestHandler
      .handle(setupRequest(input), new ObjectMapper());

    assertAll(
      () -> assertEquals(Code.OK, response.getHttpStatus()),
      () -> assertEquals(expectedOutput, response.getBody().orElse(null))
    );

    // verify the Flux RequestHandler delegating to the expected operation
    final int fluxMethodInput = 99;
    final RequestHandler<Flux<String>> fluxRequestHandler =
      findHandlerOrThrow(generatedRoutes, FLUX_PATH);
    final Response<Flux<String>> fluxResponse = fluxRequestHandler.handle(
      setupRequest(fluxMethodInput), new ObjectMapper());
    final List<String> expectedStreamingOutput = IntStream.range(0, fluxMethodInput)
      .mapToObj(HandlerInterface.fluxOutputFunction).collect(Collectors.toList());
    assertAll(
      () -> assertEquals(Code.OK, fluxResponse.getHttpStatus()),
      () -> fluxResponse.getBody()
        .orElse(Flux.empty())
        .collect(Collectors.toList())
        .subscribe(actual -> assertEquals(expectedStreamingOutput, actual))
        .dispose(),
      () -> verify(handlerSpy).returnFlux(fluxMethodInput)
    );

    // verify the void RequestHandler delegating to the expected operation
    final String voidMethodInput = "hello void";
    final RequestHandler<Object> voidRequestHandler = findHandlerOrThrow(generatedRoutes,
      VOID_PATH);
    final Response<Object> voidResponse = voidRequestHandler.handle(
      setupRequest(voidMethodInput), new ObjectMapper());
    assertAll(
      () -> assertEquals(Code.OK, voidResponse.getHttpStatus()),
      () -> assertEquals("", voidResponse.getBody().orElse(null)),
      () -> verify(handlerSpy).foo(voidMethodInput)
    );

    // verify the RequestHandler that returns null causes an exception to be thrown
    final String nullMethodInput = "hello null";
    final RequestHandler<Object> nullRequestHandler = findHandlerOrThrow(generatedRoutes,
      NULL_PATH);
    final Response<Object> nullErrorResponse = nullRequestHandler.handle(
      setupRequest(nullMethodInput), new ObjectMapper());
    assertAll(
      () -> assertEquals(Code.INTERNAL_SERVER_ERROR, nullErrorResponse.getHttpStatus()),
      () -> assertFalse(nullErrorResponse.getBody().isPresent()),
      () -> assertTrue(nullErrorResponse.getErrorMessage().get().toLowerCase(Locale.ENGLISH)
        .contains("null returned")),
      () -> verify(handlerSpy).returnNull(nullMethodInput)
    );
  }

  private static Request setupRequest(Object body) throws JsonProcessingException {
    final Request mockRequest = mock(Request.class);
    when(mockRequest.clientSentMsgpack()).thenReturn(false);
    when(mockRequest.getBody()).thenReturn(new ObjectMapper().writeValueAsString(body));
    return mockRequest;
  }

  @SuppressWarnings("unchecked")
  private static <T> RequestHandler<T> findHandlerOrThrow(Set<Route> routes, String path) {
    return routes
      .stream()
      .filter(r -> r.getPath().contains(path))
      .findAny()
      .orElseThrow(() -> new IllegalStateException("Could not find Route for path " + path))
      .getHandler();
  }

  private static final String BASE_PATH = "/base/";
  private static final String INT_PATH = "foo-int";
  private static final String OBJECT_PATH = "foo-object";
  private static final String VOID_PATH = "void";
  private static final String NULL_PATH = "null";
  private static final String FLUX_PATH = "flux";

  @Path(BASE_PATH)
  private interface HandlerInterface {

    @Path(OBJECT_PATH)
    @POST
    Compound<String> foo(Collection<Compound<String>> input);

    @Path(INT_PATH)
    @POST
    default int foo(int input) {
      return input * 2;
    }

    @Path(VOID_PATH)
    @POST
    default void foo(String input) {
    }

    @Path(NULL_PATH)
    @POST
    default String returnNull(String input) {
      return null;
    }

    @Path(FLUX_PATH)
    @POST
    default Flux<String> returnFlux(int input) {
      return Flux.range(0, input).map(fluxOutputFunction::apply);
    }

    IntFunction<String> fluxOutputFunction = Integer::toHexString;
  }
}
