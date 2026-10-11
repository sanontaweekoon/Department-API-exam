package sanon.test.department_api.services

import sanon.test.department_api.models.request.MoveDepartmentRequest
import sanon.test.department_api.models.response.MoveDepartmentResponse

interface EmployeeService {
    fun moveToDepartment(employeeId: Long, request: MoveDepartmentRequest): MoveDepartmentResponse
}