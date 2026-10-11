package sanon.test.department_api.models.response

import com.fasterxml.jackson.annotation.JsonProperty
import sanon.test.department_api.entity.Department
import sanon.test.department_api.repository.DepartmentWithCount

data class DepartmentResponse(
    @JsonProperty("code")
    val code: String,

    @JsonProperty("name")
    val name: String,

    @JsonProperty("status")
    val status: String,

    @JsonProperty("employeeCount")
    val employeeCount: Long,
)

//    fun Department.toResponse(employeeCount: Long) = DepartmentResponse (
//            code = code,
//            name = name,
//            status = status,
//            employeeCount = employeeCount,
//        )

    fun DepartmentWithCount.toResponse() = DepartmentResponse(
        code = getCode(),
        name = getName(),
        status = getStatus(),
        employeeCount = getEmployeeCount(),
    )

