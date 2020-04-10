package com.example.bootifulkotlindsl

import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.support.beans
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@SpringBootApplication
class BootifulKotlinDslApplication


fun main(args: Array<String>) {
    runApplication<BootifulKotlinDslApplication>(*args) {
        val log = LoggerFactory.getLogger("Main")

        val context = beans {
            bean {
                ApplicationRunner {
                    val customerService = ref<CustomerService>()
                    listOf("John", "Jane", "Jack")
                        .map { Customer(name = it) }
                        .forEach { customerService.insert(it) }

                    customerService.all()
                        .forEach { log.info("--> $it") }
                }
            }
        }
        addInitializers(context)
    }
}

@Service
@Transactional
class JdbcTemplateCustomerService(private val jdbcTemplate: JdbcTemplate) : CustomerService {
    override fun all(): List<Customer> = this.jdbcTemplate.query("SELECT * FROM CUSTOMERS") { rs, _ ->
        Customer(rs.getString("NAME"), rs.getLong("ID"))
    }

    override fun byId(id: Long): Customer? = this.jdbcTemplate.queryForObject("select * from CUSTOMERS where ID=?", id) { rs, _ ->
        Customer(rs.getString("NAME"), rs.getLong("ID"))
    }

    override fun insert(customer: Customer) {
        this.jdbcTemplate.execute("INSERT INTO CUSTOMERS(NAME) VALUES(?)") {
            it.setString(1, customer.name)
            it.execute()
        }
    }
}

interface CustomerService {
    fun all(): List<Customer>
    fun byId(id: Long): Customer?
    fun insert(customer: Customer)
}

data class Customer(val name: String, var id: Long? = null)