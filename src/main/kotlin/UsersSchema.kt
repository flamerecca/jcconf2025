package com.example

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

@Serializable
data class ExposedUser(val name: String, val age: Int)

object Users : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", length = 50)
    val age = integer("age")

    override val primaryKey = PrimaryKey(id)
}

class UserService(database: Database) {
    init {
        transaction(database) {
            SchemaUtils.create(Users)
        }
    }

    fun create(user: ExposedUser): Int = transaction {
        Users.insert {
            it[name] = user.name
            it[age] = user.age
        }[Users.id]
    }

    fun read(id: Int): ExposedUser? {
        return transaction {
            Users.selectAll()
                .where { Users.id eq id }
                .map { ExposedUser(it[Users.name], it[Users.age]) }
                .singleOrNull()
        }
    }

    fun update(id: Int, user: ExposedUser) {
        transaction {
            Users.update({ Users.id eq id }) {
                it[name] = user.name
                it[age] = user.age
            }
        }
    }

    fun delete(id: Int) {
        transaction {
            Users.deleteWhere { Users.id.eq(id) }
        }
    }
}

