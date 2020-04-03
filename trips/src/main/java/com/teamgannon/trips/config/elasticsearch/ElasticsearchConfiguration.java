package com.teamgannon.trips.config.elasticsearch;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.NodeBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 *
 * Created by larrymitchell on 2017-01-25.
 */

@Slf4j
@Configuration
@EnableElasticsearchRepositories(basePackages = "com.teamgannon.trips.elasticsearch.repository")
public class ElasticsearchConfiguration {

    @Bean
    public Client client() {
        try {
            final Path tmpDir = Files.createTempDirectory(Paths.get(System.getProperty("java.io.tmpdir")), "elasticsearch_data");
            log.debug(tmpDir.toAbsolutePath().toString());

            // @formatter:off

            final Settings.Builder elasticsearchSettings =
                    Settings.settingsBuilder().put("http.enabled", "false")
                            .put("path.data", "./datadir")
                            .put("path.home", "./datadir");

            return new NodeBuilder()
                    .local(true)
                    .settings(elasticsearchSettings)
                    .node()
                    .client();

            // @formatter:on
        } catch (final IOException ioex) {
            log.error("Cannot create temp dir", ioex);
            throw new RuntimeException();
        }
    }

    @Bean
    public ElasticsearchOperations elasticsearchTemplate() {
        return new ElasticsearchTemplate(client());
    }


}
