package sanon.test.department_api.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import sanon.test.department_api.models.response.DepartmentResponse
import sanon.test.department_api.models.response.DepartmentSummaryResponse
import sanon.test.department_api.services.DepartmentService

@RestController
@RequestMapping("/api")
class DepartmentController (private val departmentService: DepartmentService) {

    @GetMapping("/departments")
    fun findAll(): List<DepartmentResponse> = departmentService.findAll()

    @GetMapping("/departments/{code}/summary")
    fun getDepartmentSummary(
        @PathVariable("code") code: String,
    ): DepartmentSummaryResponse = departmentService.getDepartmentSummary(code)
}