/*
 * Copyright (C) 2021 CLARIN ERIC <clarin@clarin.eu>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.clarin.cmdi.vlo.api.data;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.HealthStatus;
import com.google.common.collect.Lists;
import eu.clarin.cmdi.vlo.api.configuration.VloElasticsearchConfiguration;
import eu.clarin.cmdi.vlo.data.model.VloRecord;
import eu.clarin.cmdi.vlo.data.model.VloRecord.Resource;
import eu.clarin.cmdi.vlo.elasticsearch.VloRecordRepository;
import java.io.IOException;
import java.util.Collection;
import java.util.function.BooleanSupplier;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchClientAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.ReactiveElasticsearchClientAutoConfiguration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.repository.config.EnableReactiveElasticsearchRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@ExtendWith(SpringExtension.class)
@Profile("elastic-test")
@ContextConfiguration(classes = VloElasticsearchConfiguration.class)
@TestPropertySource(locations = {"classpath:elastic-test.properties"})
@EnabledIf(expression = "#{environment.acceptsProfiles('elastic-test')}", loadContext = true)
@EnableReactiveElasticsearchRepositories(basePackageClasses = VloRecordRepository.class)
@ImportAutoConfiguration({ElasticsearchClientAutoConfiguration.class, ReactiveElasticsearchClientAutoConfiguration.class, ElasticsearchDataAutoConfiguration.class})
@Slf4j
public class VloRecordRepositoryIntegrationTest {
    
    @Autowired
    private VloRecordRepository instance;

//    @Autowired
//    private RestHighLevelClient elasticsearchClient;
    @Autowired
    private ElasticsearchOperations operations;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    private final Collection<String> insertedIds = Lists.newCopyOnWriteArrayList();

    @AfterEach
    public void cleanUp() {
        if (checkElasticConnection().getAsBoolean()) {
            Flux.fromIterable(insertedIds)
                    .parallel()
                    .doOnNext(s -> {
                        log.debug("Cleanup: removing {}", s);
                    })
                    .doOnError(e -> {
                        log.warn("Error while cleaning up from index", e);
                    })
                    .flatMap(id -> {
                        return instance.deleteById(id)
                                .then(Mono.fromCallable(() -> insertedIds.remove(id)));
                    })
                    .then()
                    .block();
        }
    }

    /**
     * Test of sendToIndex method, of class VloRecordIndexService.
     */
    @Test
    public void testSave() throws IOException {
        Assumptions.assumeTrue(checkElasticConnection(), "ElasticSearch not running. SKIPPING!");

        final VloRecord inputRecord = VloRecord.builder()
                .id("id123")
                .dataRoot("testDataRoot")
                .profileId("clarin_profile_id")
                .selflink("http://repo.eu/id123")
                .resources(Lists.newArrayList(
                        Resource.builder().id("resource1").mediaType("text/plain").ref("http://repo.eu/id123/resource1").build(),
                        Resource.builder().id("resource2").mediaType("text/plain").ref("http://repo.eu/id123/resource2").build()
                ))
                .build();

        inputRecord.getFields().put("test", Lists.newArrayList("test1", "test2"));
        inputRecord.getResources().add(Resource.builder().id("resource1").mediaType("text/plain").ref("http://repo.eu/id123/resource1").build());

        final Mono<VloRecord> result = instance.save(inputRecord);
        final VloRecord response = result
                .doOnError((e) -> {
                    fail("Error in IndexResponse mono", e);
                })
                .block();

        assertNotNull(response);
        insertedIds.add(response.getId());

        VloRecord retrieved = operations.get(inputRecord.getId(), VloRecord.class);

//        GetResponse getResponse = elasticsearchClient.get(
//                Requests.getRequest("record")
//                        .id(inputRecord.getId()),
//                RequestOptions.DEFAULT);
        assertEquals(inputRecord.getId(), retrieved.getId());
        assertEquals(inputRecord.getDataRoot(), retrieved.getDataRoot());
    }

    private BooleanSupplier checkElasticConnection() {
        return () -> {
            try {
                return elasticsearchClient.cluster().health().status() != HealthStatus.Red;
            } catch (ElasticsearchException | IOException ex) {
                log.warn("Error while retrieving clust health status", ex);
                return false;
            }
        };
    }

//    @Configuration
//    public static class TestElasticsearchConfig extends VloElasticsearchConfiguration {
//
//        @Bean
//        @Override
//        public ClientConfiguration clientConfiguration() {
//            return ClientConfiguration.builder()
//                    .connectedTo("localhost:9200")
//                    .build();
//
//        }
//    }

//
//    @Configuration
//    public static class TestElasticsearchConfig extends AbstractElasticsearchConfiguration {
//
//        @Autowired
//        private ClientConfiguration clientConfiguration;
//
//        @Override
//        @Bean
//        public RestHighLevelClient elasticsearchClient() {
//            return RestClients.create(clientConfiguration).rest();
//        }
//    }
}
