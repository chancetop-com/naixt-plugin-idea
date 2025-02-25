package com.chancetop.naixt.plugin.idea.agent.internal;

import com.chancetop.naixt.plugin.idea.agent.AgentServerService;
import com.intellij.ide.plugins.cl.PluginClassLoader;
import core.framework.http.HTTPClient;
import core.framework.internal.bean.BeanClassValidator;
import core.framework.internal.web.bean.RequestBeanWriter;
import core.framework.internal.web.bean.ResponseBeanReader;
import core.framework.internal.web.service.WebServiceClient;
import core.framework.internal.web.service.WebServiceInterfaceValidator;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

/**
 * @author stephen
 */
public class ApiClient {
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(ApiClient.class);
    private final BeanClassValidator beanClassValidator = new BeanClassValidator();
    private final List<String> jarPaths;

    public ApiClient() {
        var jars = List.of(
                "agent-service-interface",
                "core-ng",
                "core-ng-api");
        jarPaths = getJarClasspath(jars);
    }

    public  <T> T createClient(Class<T> t, String endpoint) {
        var client = HTTPClient.builder().timeout(Duration.ofSeconds(30)).build();
        var writer = new RequestBeanWriter();
        var reader = new ResponseBeanReader();
        logger.info("create web service client, interface={}, serviceURL={}", t.getCanonicalName(), endpoint);
        var validator = new WebServiceInterfaceValidator(t, beanClassValidator);
        validator.requestBeanWriter = writer;
        validator.responseBeanReader = reader;
        validator.validate();
        return new WebServiceClientBuilder<>(t, new WebServiceClient(endpoint, client, writer, reader), jarPaths.toArray(new String[0])).build();
    }

    private List<String> getJarClasspath(List<String> jars) {
        var dir = ((PluginClassLoader) AgentServerService.class.getClassLoader()).getLibDirectories().stream().filter(v -> v.endsWith("naixt-plugin-idea/lib")).findFirst().orElseThrow();
        try (var walk = Files.walk(dir, 1)) {
            return walk.filter(Files::isRegularFile).map(Path::toString).filter(v -> jars.stream().anyMatch(v::contains)).toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
