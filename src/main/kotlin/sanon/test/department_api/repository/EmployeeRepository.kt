package sanon.test.department_api.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import sanon.test.department_api.entity.Employee
import java.math.BigDecimal

interface DepartmentStats {
    fun getEmployeeCount(): Long
    fun getActiveCount(): Long
    fun getInactiveCount(): Long
    fun getTotalSalary(): BigDecimal
}

@Repository
interface EmployeeRepository: JpaRepository<Employee, Long> {
    fun countByDepartmentId(departmentId: Long): Long

    @Query(value = """
    SELECT COUNT(*) AS employeeCount,
           COUNT(*) FILTER (WHERE status = 'ACTIVE') AS activeCount,
           COUNT(*) FILTER (WHERE status = 'INACTIVE') AS inactiveCount,
           COALESCE(SUM(salary), 0) AS totalSalary
    FROM employees
    WHERE department_id = :departmentId
    """, nativeQuery = true)
    fun getStats(departmentId: Long): DepartmentStats

}