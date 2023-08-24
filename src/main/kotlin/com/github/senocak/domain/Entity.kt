package com.github.senocak.domain

import com.github.senocak.util.RoleName
import org.hibernate.annotations.GenericGenerator
import java.io.Serializable
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.persistence.*

@MappedSuperclass
open class BaseDomain(
        @Id
        @GeneratedValue(generator = "UUID")
        @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
        @Column(name = "id", updatable = false, nullable = false)
        var id: String? = null,
        @Column var createdAt: Date = Date(),
        @Column var updatedAt: Date = Date()
): Serializable{
        @PrePersist
        protected open fun prePersist() {
                id = UUID.randomUUID().toString()
        }
}

@Entity
@Table(name = "users", uniqueConstraints = [
        UniqueConstraint(columnNames = ["username"]),
        UniqueConstraint(columnNames = ["email"])
])
class User(
        @Column var name: String,
        @Column var username: String,
        @Column var email : String,
        @Column var password : String?,
        @JoinTable(name = "user_roles",
                joinColumns = [JoinColumn(name = "user_id")],
                inverseJoinColumns = [JoinColumn(name = "role_id")]
        )
        @ManyToMany(fetch = FetchType.LAZY)
        var roles: Set<Role> = HashSet(),

        @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
        @JoinColumn(
                name = "package_id",
                referencedColumnName = "id",
                foreignKey = ForeignKey(name = "fk_user_package_package_id")
        )
        var `package`: Package? = null
): BaseDomain()

@Entity
@Table(name = "roles")
class Role(@Column @Enumerated(EnumType.STRING) var name: RoleName? = null): BaseDomain()

@Entity
@Table(name = "packages")
class Package(
        @Column(nullable = false, unique = true) var name: String,
        @Column(nullable = false) var limitPer: Long,
        @Column(nullable = false) var type: String = TimeUnit.MINUTES.name,
        @OneToMany(mappedBy = "package", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
        var users: List<User> = ArrayList()
): BaseDomain()
