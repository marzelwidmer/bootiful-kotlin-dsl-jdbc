package com.example.bootifulkotlindsl

import org.jetbrains.exposed.spring.SpringTransactionManager
import org.jetbrains.exposed.sql.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.support.beans
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
class BootifulKotlinDslApplication


fun main(args: Array<String>) {
    runApplication<BootifulKotlinDslApplication>(*args) {
        val log = LoggerFactory.getLogger("Main")

        val context = beans {
            bean { SpringTransactionManager(ref())} // kotlin dsl
            bean {
                ApplicationRunner {
                    val customerService = ref<CustomerService>()
                    listOf("John", "Jane", "Jack")
                        .map { Customer(name = it) }
                        .forEach { customerService.insert(it) }

                    customerService.all()
                        .forEach { log.info("--> $it") }

                    // kotlin dsl
                    val personService = ref<PersonService>()
                    listOf("Doe", "Norris", "Bauer")
                        .map { Person(name = it) }
                        .forEach { personService.insert(it) }

                    personService.all()
                        .forEach { log.info("DSL --> $it") }
                }
            }
        }
        addInitializers(context)
    }
}

@RestController
class Api(private val customerService: CustomerService, private val personService: PersonService){

    @GetMapping(value = ["/customers"])
    fun getAllCustomers() = customerService.all()

    @GetMapping(value = ["/customers/{id}"])
    fun getCustomerById(@PathVariable id : String) = customerService.byId(id.toLong())

    @GetMapping(value = ["/persons"])
    fun getAllPersons() = personService.all()

    @GetMapping(value = ["/persons/{id}"])
    fun getPersonById(@PathVariable id : String) = personService.byId(id.toLong())
}


//   _  __     _   _ _         ____  ____  _
//  | |/ /___ | |_| (_)_ __   |  _ \/ ___|| |
//  | ' // _ \| __| | | '_ \  | | | \___ \| |
//  | . \ (_) | |_| | | | | | | |_| |___) | |___
//  |_|\_\___/ \__|_|_|_| |_| |____/|____/|_____|
//
@Service
@Transactional
class ExposedPersonService(private val transactionalTemplate: TransactionTemplate) : PersonService, InitializingBean {

    override fun afterPropertiesSet() {
        transactionalTemplate.execute {
            SchemaUtils.create(Persons)
        }
    }

    override fun all(): List<Person> = Persons.selectAll()
        .map { Person(it[Persons.name], it[Persons.id]) }

    override fun byId(id: Long): Person? = Persons
        .select { Persons.id.eq(id) }
        .map { Person(it[Persons.name], it[Persons.id]) }
        .firstOrNull()

    override fun insert(person: Person) {
        Persons.insert { it[Persons.name] = person.name }
    }
}

object Persons : Table() {
    val id = long("ID").autoIncrement().primaryKey()
    val name = varchar("NAME", 255)
}

interface PersonService {
    fun all(): List<Person>
    fun byId(id: Long): Person?
    fun insert(person: Person)
}

data class Person(val name: String, var id: Long? = null)

//       _     _ _         _____                    _       _
//      | | __| | |__   ____   _|__ _ __ ___  _ __ | | __ _| |_ ___
//   _  | |/ _` | '_ \ / __|| |/ _ \ '_ ` _ \| '_ \| |/ _` | __/ _ \
//  | |_| | (_| | |_) | (__ | |  __/ | | | | | |_) | | (_| | |_  __/
//   \___/ \__,_|_.__/ \___||_|\___|_| |_| |_| .__/|_|\__,_|\__\___|
//                                           |_|

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


