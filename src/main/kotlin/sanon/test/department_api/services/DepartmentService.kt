package sanon.test.department_api.services

import sanon.test.department_api.models.response.DepartmentResponse
import sanon.test.department_api.models.response.DepartmentSummaryResponse

interface DepartmentService {
    fun findAll(): List<DepartmentResponse>
    fun getDepartmentSummary(code: String): DepartmentSummaryResponse
}