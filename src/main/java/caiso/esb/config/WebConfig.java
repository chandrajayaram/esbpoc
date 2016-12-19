package caiso.esb.config;

import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import java.util.Arrays;

/**
 * Configuration of servlet used in camel route to take inbound service requests
 */
@Configuration
public class WebConfig implements ServletContextInitializer {
    private static final Logger logger = LogManager.getLogger(WebConfig.class);

    @Resource
    private Environment env;

// public ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        logger.info("Web application configuration, using profiles: {}", Arrays.toString(env.getActiveProfiles()));
        initCamelServlet(servletContext);
        logger.info("Web application fully configured");
    }

    /**
     * Initializes Metrics.
     */
    private void initCamelServlet(ServletContext servletContext) {
        ServletRegistration.Dynamic metricsAdminServlet =
                servletContext.addServlet("CAISOESBServlet", new CamelHttpTransportServlet());

        metricsAdminServlet.addMapping("/*");
        metricsAdminServlet.setAsyncSupported(true);
        metricsAdminServlet.setLoadOnStartup(1);
    }
}
