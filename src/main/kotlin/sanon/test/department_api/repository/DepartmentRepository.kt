package sanon.test.department_api.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import sanon.test.department_api.entity.Department
import org.springframework.data.jpa.repository.Query

interface DepartmentWithCount {
    fun getCode(): String
    fun getName(): String
    fun getStatus(): String
    fun getEmployeeCount(): Long
}

@Repository
interface DepartmentRepository: JpaRepository<Department, Long> {
    fun findByCode(code: String): Department?

    @Query(value = """
        SELECT d.code      AS code,
               d.name      AS name,
               d.status    AS status,
               COUNT(e.id) AS employeeCount
        FROM departments d
        LEFT JOIN employees e ON e.department_id = d.id
        GROUP BY d.id, d.code, d.name, d.status
        ORDER BY d.code
    """, nativeQuery = true)
    fun findAllWithEmployeeCount(): List<DepartmentWithCount>
}




