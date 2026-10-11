package sanon.test.department_api.controller

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import sanon.test.department_api.models.request.MoveDepartmentRequest
import sanon.test.department_api.models.response.MoveDepartmentResponse
import sanon.test.department_api.services.EmployeeService

@RestController
@RequestMapping("/api/employees")
class EmployeeController(private val employeeService: EmployeeService) {

    @PutMapping("/{employeeId}/department")
    fun moveToDepartment(
        @PathVariable employeeId: Long,
        @RequestBody request: MoveDepartmentRequest,
    ): MoveDepartmentResponse = employeeService.moveToDepartment(employeeId, request)
}