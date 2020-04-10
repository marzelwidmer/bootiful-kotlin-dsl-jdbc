package com.example.bootifulkotlindsl

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.PreparedStatement

@SpringBootApplication
class BootifulKotlinDslApplication

fun main(args: Array<String>) {
    runApplication<BootifulKotlinDslApplication>(*args)
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