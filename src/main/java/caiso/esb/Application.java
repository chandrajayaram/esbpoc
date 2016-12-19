package caiso.esb;

import caiso.esb.config.ApplicationProperties;
import org.apache.camel.spring.boot.CamelSpringBootApplicationController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;

@SpringBootApplication
@ComponentScan({"caiso.esb", "caiso.camel", "caiso.jms.config"})
public class Application implements CommandLineRunner {

    private static final Logger logger = LogManager.getLogger(Application.class);
    @Resource
    private ConfigurableApplicationContext applicationContext;
    @Resource
    private Environment environment;
    @Resource
    private ApplicationProperties applicationProperties;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    // public ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    public void run(String... args) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (String profile : environment.getActiveProfiles()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(profile);
        }
        logger.info("Running {} v{} builtBy: {} ", applicationProperties.getName(), applicationProperties.getVersion(), applicationProperties.getBuildId());
        logger.info("Using spring-boot profile(s) [ {} ]...", sb.toString());

        // to block main thread from exit if no resources/threads started
        CamelSpringBootApplicationController applicationController =
                applicationContext.getBean(CamelSpringBootApplicationController.class);
        applicationController.run();

    }
}
