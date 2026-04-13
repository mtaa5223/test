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
        }

        dataSource = HikariDataSource(hikariConfig)
        context = DSL.using(dataSource, SQLDialect.POSTGRES)

        Flyway.configure()
            .dataSource(dataSource)
            .baselineOnMigrate(true)
            .load()
            .migrate()
    }
}
