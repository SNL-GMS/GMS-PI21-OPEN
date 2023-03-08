package gms.shared.spring.utilities.aspect;

import net.logstash.logback.argument.StructuredArguments;
import net.logstash.logback.marker.Markers;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * Class that logs information about method parameters and execution time.
 * <p>
 * Logs at different levels based on time that wrapped method takes to execute.
 */
@Component
@Aspect
public class LoggingAspect {

  @Around("execution(* gms.shared..*(..)) && "
    + "(@annotation(org.springframework.web.bind.annotation.PostMapping))")
  public Object logPostMethod(ProceedingJoinPoint joinPoint) throws Throwable {
    logRequestBody(joinPoint);
    return LoggingAspect.logTimeMethod(joinPoint);
  }

  private void logRequestBody(ProceedingJoinPoint joinPoint) {
    var logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
    try {
      MethodSignature signature = (MethodSignature) joinPoint.getSignature();
      String[] parameterNames = signature.getParameterNames();
      Object[] parameterValues = joinPoint.getArgs();
      var requestParams = new StringBuilder("");
      for (var i = 0; i < parameterValues.length; i++) {
        if (!parameterNames[i].startsWith("model") && !parameterNames[i].startsWith("allRequestParams")
          && parameterValues[i] != null) {
          if (requestParams.length() > 0) {
            requestParams.append(";");
          }
          requestParams.append(parameterNames[i] + ": ");
          requestParams.append(parameterValues[i].toString());
        }
      }

      var logMessage = String.format("Entering %s -- %s", joinPoint.getStaticPart().getSignature().toString(), requestParams);
      logger.info(logMessage);

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
  }

  public static Object logTimeMethod(ProceedingJoinPoint joinPoint) throws Throwable {
    var logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
    var start = Instant.now();

    Object retVal = joinPoint.proceed();
    var end = Instant.now();

    var methodPlusArgs = new StringBuilder()
      .append(joinPoint.getStaticPart().getSignature().toString())
      .toString();
    logger.info(Markers.aggregate(
          Markers.append("startTime", start.toEpochMilli()),
          Markers.append("endTime", end.toEpochMilli())),
      "{} ran in {} milliseconds",
      StructuredArguments.v("methodName", methodPlusArgs),
      StructuredArguments.v("elapsedTime", Duration.between(start, end).toMillis()));

    return retVal;
  }
}
