package gms.shared.frameworks.common.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a component interface or class to provide it a name.
 * This can be used to look up configuration for the component.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Component {

  /**
   * Obtain the component's name
   *
   * @return String containing the component's name, not null
   */
  String value();
}
