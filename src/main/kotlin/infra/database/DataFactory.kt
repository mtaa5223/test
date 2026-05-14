package com.example.infra.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.ApplicationConfig
import org.flywaydb.core.Flyway
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL

object DataFactory {
    private lateinit var dataSource: HikariDataSource
    lateinit var context: DSLContext
        private set

    fun init(config: ApplicationConfig) {
        val db = config.config("database")
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = db.property("url").getString()
            username = db.property("user").getString()
            password = db.property("password").getString()
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = db.property("poolSize").getString().toInt()

            // Hikari가 startup에 실제 connection을 강제로 만들지 않도록 한다.
            // Neon serverless가 cold-start 중일 때 module() 블록이 30초 이상 멈추는 걸 방지.
            initializationFailTimeout = -1
            connectionTimeout = 30_000

            // Neon은 TLS 필수.
            addDataSourceProperty("sslmode", "require")
        }

        dataSource = HikariDataSource(hikariConfig)
        context = DSL.using(dataSource, SQLDialect.POSTGRES)
    }

    fun runMigrations() {
        Flyway.configure()
            .dataSource(dataSource)
            .baselineOnMigrate(true)
            .load()
            .migrate()
    }
}
