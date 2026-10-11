package sanon.test.department_api

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import org.testcontainers.utility.MountableFile

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    fun postgresContainer(): PostgreSQLContainer =

        PostgreSQLContainer(DockerImageName.parse("postgres:17.11-alpine"))

            .withCopyFileToContainer(
                MountableFile.forHostPath("dataset.sql"),
                "/docker-entrypoint-initdb.d/01-dataset.sql",
            )
}