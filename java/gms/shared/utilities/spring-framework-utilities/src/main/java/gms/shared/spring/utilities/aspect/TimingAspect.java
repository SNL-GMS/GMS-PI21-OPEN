package gms.shared.spring.utilities.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Class that logs information about method parameters and execution time.
 * <p>
 * Logs at different levels based on time that wrapped method takes to execute.
 */
@Component
@Aspect
@Profile("timing")
public class TimingAspect {

  @Around("execution(* gms.shared.*.database.connector..*(..)))")
  public Object logDatabaseMethod(ProceedingJoinPoint joinPoint) throws Throwable {
    return LoggingAspect.logTimeMethod(joinPoint);
  }

  @Around("@annotation(gms.shared.spring.utilities.aspect.Timing)")
  public Object logTimingMethod(ProceedingJoinPoint joinPoint) throws Throwable {
    return LoggingAspect.logTimeMethod(joinPoint);
  }
}
