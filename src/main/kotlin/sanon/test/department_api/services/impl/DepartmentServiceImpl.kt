package sanon.test.department_api.services.impl

import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import sanon.test.department_api.exception.NotFoundException
import sanon.test.department_api.models.response.DepartmentResponse
import sanon.test.department_api.models.response.DepartmentSummaryResponse
import sanon.test.department_api.models.response.toResponse
import sanon.test.department_api.repository.DepartmentRepository
import sanon.test.department_api.repository.EmployeeRepository
import sanon.test.department_api.services.DepartmentService
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class DepartmentServiceImpl  (
    private val departmentRepository: DepartmentRepository,
    private val employeeRepository: EmployeeRepository
) : DepartmentService {

//    override fun findAll(): List<DepartmentResponse> =
//        departmentRepository.findAll(Sort.by("code"))
//            .map { it.toResponse(employeeRepository.countByDepartmentId(it.id!!)) }

    override fun findAll(): List<DepartmentResponse> =
        departmentRepository.findAllWithEmployeeCount().map { it.toResponse() }

    override fun getDepartmentSummary(code: String): DepartmentSummaryResponse {
        val department = departmentRepository.findByCode(code) ?: throw NotFoundException("DEPARTMENT_NOT_FOUND", "Department $code not found")

        val stats = employeeRepository.getStats(department.id!!)
        val total = stats.getTotalSalary().setScale(2)
        val average = if (stats.getEmployeeCount() == 0L) BigDecimal("0.00")
        else total.divide(BigDecimal(stats.getEmployeeCount()), 2, RoundingMode.HALF_UP)

        return DepartmentSummaryResponse(
            department.code,
            department.name,
            status = department.status,
            employeeCount = stats.getEmployeeCount(),
            activeEmployeeCount = stats.getActiveCount(),
            inactiveEmployeeCount = stats.getInactiveCount(),
            totalSalary = total,
            averageSalary = average,

        )
    }
}