package gms.shared.user;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"gms.shared.user", "gms.shared.spring"})
@EntityScan(basePackages = "gms.shared.user.preferences.dao")
public class UserManagerApplication {

  public static void main(String[] args) {
    new SpringApplicationBuilder(UserManagerApplication.class)
      .run(args);
  }
}