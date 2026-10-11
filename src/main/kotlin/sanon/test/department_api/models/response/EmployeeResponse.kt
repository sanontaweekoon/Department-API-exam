package sanon.test.department_api.models.response

import sanon.test.department_api.entity.Employee
import java.math.BigDecimal
import java.time.Instant

data class EmployeeResponse (
    val employeeCode: String,
    val fullName: String,
    val salary: BigDecimal,
    val status: String,
    val departmentId: Long,
    val updatedAt: Instant,
){
    companion object {
        fun from(employee: Employee, instanceName: String): EmployeeResponse = EmployeeResponse (
            employeeCode = employee.employeeCode,
            fullName = employee.fullName,
            salary = employee.salary,
            status = employee.status,
            departmentId = employee.departmentId,
            updatedAt = employee.updatedAt,
            )
    }
}