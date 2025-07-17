package config;

import org.apache.fop.apps.FopFactory;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.*;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

@EnableWebMvc
@Configuration
@ComponentScan( value = { "controllers", "exceptions", "domain" })
@PropertySource("classpath:/application.properties")
public class AppConfig {

    private static final Logger logger = Logger.getLogger(AppConfig.class);

    @Value("classpath:fop.conf")
    private Resource conf;

    @Bean("fopFactory")
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public FopFactory getFopFactory() {
        try {
            logger.info("Starting new FopFactory");
            logger.info("Reading config file: " + conf.getURI());
            return FopFactory.newInstance(new File(conf.getURI()));
        } catch (IOException | SAXException e) {
            logger.debug("Could not load config file");
            throw new RuntimeException("Could not load config file!");
        }
    }
}
