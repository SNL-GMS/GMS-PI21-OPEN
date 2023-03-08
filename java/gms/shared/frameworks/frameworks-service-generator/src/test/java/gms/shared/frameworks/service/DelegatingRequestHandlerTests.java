package gms.shared.frameworks.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.frameworks.common.HttpStatus.Code;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Type;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DelegatingRequestHandlerTests {

  private static final Type intType = Integer.class;

  @Test
  void testCreate() {
    final Type requestType = intType;
    final Function<Integer, Double> handlerOperation = i -> i / 2.0;
    final DelegatingRequestHandler delegatingRequestHandler = DelegatingRequestHandler
      .create(requestType, handlerOperation);

    assertAll(
      () -> assertNotNull(delegatingRequestHandler),
      () -> assertEquals(requestType, delegatingRequestHandler.getRequestType()),
      () -> assertEquals(handlerOperation, delegatingRequestHandler.getHandlerOperation())
    );
  }

  @Test
  void testHandle() {
    class Handler {
      public int foo(int x) {
        return x / 2;
      }
    }
    final Handler handlerSpy = spy(new Handler());
    final DelegatingRequestHandler<Integer, Integer> requestHandler = DelegatingRequestHandler
      .create(intType, handlerSpy::foo);

    final int param = 8;
    final Request mockRequest = mock(Request.class);
    when(mockRequest.clientSentMsgpack()).thenReturn(false);
    when(mockRequest.getBody()).thenReturn(String.valueOf(param));

    final Response<Integer> response = requestHandler.handle(mockRequest, new ObjectMapper());

    // Verifies handler logic is delegated
    verify(handlerSpy).foo(param);

    // Verify expected response
    assertAll(
      () -> assertNotNull(response),
      () -> assertEquals(Code.OK, response.getHttpStatus()),
      () -> assertEquals(new Handler().foo(param), (int) response.getBody().orElse(Integer.MIN_VALUE))
    );
  }

  @Test
  void testHandleDeserializeFailsExpectClientError() {
    final DelegatingRequestHandler<Integer, Double> handler = DelegatingRequestHandler
      .create(intType, i -> i / 4.0);

    final Request mockRequest = mock(Request.class);
    when(mockRequest.clientSentMsgpack()).thenReturn(false);
    when(mockRequest.getBody()).thenReturn("ThisIsNotAnInteger");

    final Response<Double> response = handler.handle(mockRequest, new ObjectMapper());

    assertAll(
      () -> assertNotNull(response),
      () -> assertEquals(Code.BAD_REQUEST, response.getHttpStatus()),
      () -> assertTrue(response.getErrorMessage().orElse("").startsWith(
        "Could not deserialize request body into an instance of this route handler's request type")));
  }

  @Test
  void testHandleHandlerOperationFailsExpectServerError() {
    final DelegatingRequestHandler<Integer, Double> handler = DelegatingRequestHandler
      .create(intType, i -> {
        throw new IllegalArgumentException("");
      });

    final Request mockRequest = mock(Request.class);
    when(mockRequest.clientSentMsgpack()).thenReturn(false);
    when(mockRequest.getBody()).thenReturn("7");

    final Response<Double> response = handler.handle(mockRequest, new ObjectMapper());

    // Verify expected response
    assertAll(
      () -> assertNotNull(response),
      () -> assertEquals(Code.INTERNAL_SERVER_ERROR, response.getHttpStatus()),
      () -> assertTrue(
        response.getErrorMessage().orElse("").contains("handlerOperation failed"))
    );
  }

  @Test
  void testHandlerHandlerOperationThrowsInvalidInputExceptionExpectClientError() {
    final String errorMsg = "Invalid input";
    final DelegatingRequestHandler<Integer, Double> handler = DelegatingRequestHandler
      .create(intType, i -> {
        throw new InvalidInputException(errorMsg);
      });

    final Request mockRequest = mock(Request.class);
    when(mockRequest.clientSentMsgpack()).thenReturn(false);
    when(mockRequest.getBody()).thenReturn("7");

    final Response<Double> response = handler.handle(mockRequest, new ObjectMapper());

    // Verify expected response
    assertAll(
      () -> assertNotNull(response),
      () -> assertEquals(Code.BAD_REQUEST, response.getHttpStatus()),
      () -> assertTrue(
        response.getErrorMessage().orElse("").contains(errorMsg))
    );
  }

  @Test
  void testHandleValidatesInputs() {
    final DelegatingRequestHandler<Integer, Double> handler = DelegatingRequestHandler
      .create(intType, i -> i / 4.0);

    verifyNullPointerException(() -> handler.handle(null, new ObjectMapper()),
      "Request can't be null");
    verifyNullPointerException(() -> handler.handle(mock(Request.class), null),
      "ObjectMapper can't be null");
  }

  private static void verifyNullPointerException(Executable executable, String message) {
    assertTrue(assertThrows(NullPointerException.class, executable).getMessage().contains(message));
  }
}
