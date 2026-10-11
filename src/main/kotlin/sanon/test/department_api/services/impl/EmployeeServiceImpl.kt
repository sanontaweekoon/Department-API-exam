package sanon.test.department_api.services.impl

import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sanon.test.department_api.exception.BadRequestException
import sanon.test.department_api.exception.ConflictException
import sanon.test.department_api.exception.DepartmentMoveFailedException
import sanon.test.department_api.exception.NotFoundException
import sanon.test.department_api.models.request.MoveDepartmentRequest
import sanon.test.department_api.models.response.MoveDepartmentResponse
import sanon.test.department_api.repository.DepartmentRepository
import sanon.test.department_api.repository.EmployeeRepository
import sanon.test.department_api.services.EmployeeService
import java.time.Instant

@Service
class EmployeeServiceImpl(
    private val employeeRepository: EmployeeRepository,
    private val departmentRepository: DepartmentRepository,
) : EmployeeService {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(EmployeeServiceImpl::class.java)
        private const val ACTIVE = "ACTIVE"
    }

    @Transactional
    override fun moveToDepartment(employeeId: Long, request: MoveDepartmentRequest): MoveDepartmentResponse {
        if (employeeId <= 0) {
            throw BadRequestException("employeeId must be a positive integer")
        }
        val departmentCode = request.departmentCode
        if (departmentCode !is String || departmentCode.isBlank()) {
            throw BadRequestException("departmentCode must be a non-empty string")
        }

        val employee = employeeRepository.findByIdOrNull(employeeId)
            ?: throw NotFoundException("EMPLOYEE_NOT_FOUND", "Employee $employeeId not found")

        val target = departmentRepository.findByCode(departmentCode)
            ?: throw NotFoundException("DEPARTMENT_NOT_FOUND", "Department $departmentCode not found")

        if (employee.status != ACTIVE) {
            throw ConflictException("EMPLOYEE_INACTIVE", "Employee $employeeId is inactive")
        }
        if (target.status != ACTIVE) {
            throw ConflictException("DEPARTMENT_INACTIVE", "Department $departmentCode is inactive")
        }

        val targetId = target.id!!
        if (employee.departmentId == targetId) {
            return MoveDepartmentResponse(employeeId, target.code)
        }

        employee.departmentId = targetId
        employee.updatedAt = Instant.now()
        try {
            employeeRepository.saveAndFlush(employee)
        } catch (e: DataAccessException) {
            LOGGER.error("Failed to move employee {} to {}", employeeId, departmentCode, e)
            throw DepartmentMoveFailedException(e)
        }

        return MoveDepartmentResponse(employeeId, target.code)
    }
}