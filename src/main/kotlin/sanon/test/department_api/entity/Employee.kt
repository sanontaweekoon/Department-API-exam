package sanon.test.department_api.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant
import org.hibernate.annotations.DynamicUpdate

@DynamicUpdate
@Entity
@Table(name = "employees")
class Employee (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "employee_code", nullable = false, length = 20)
    var employeeCode: String,

    @Column(name = "full_name", nullable = false, length = 100)
    var fullName: String,

    @Column(nullable = false, precision  = 12, scale = 2)
    var salary: BigDecimal,

    @Column(nullable = false, length = 10)
    var status: String,

    @Column(nullable = false, length = 100)
    var departmentId: Long,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,
)