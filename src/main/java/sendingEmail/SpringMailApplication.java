package sendingEmail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication
@EnableAsync
public class SpringMailApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringMailApplication.class, args);
    }
}
