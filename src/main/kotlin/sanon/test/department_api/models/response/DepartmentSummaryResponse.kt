package sanon.test.department_api.models.response

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

data class DepartmentSummaryResponse (
    @JsonProperty("departmentCode")
    val departmentCode: String,

    @JsonProperty("departmentName")
    val departmentName: String,

    @JsonProperty("status")
    val status: String,

    @JsonProperty("employeeCount")
    val employeeCount: Long,

    @JsonProperty("activeEmployeeCount")
    val activeEmployeeCount: Long,

    @JsonProperty("inactiveEmployeeCount")
    val inactiveEmployeeCount: Long,

    @JsonProperty("totalSalary")
    val totalSalary: BigDecimal,

    @JsonProperty("averageSalary")
    val averageSalary: BigDecimal,


    )
    fun DepartmentSummaryResponse.toResponse() = DepartmentSummaryResponse (
        departmentCode = departmentCode,
        departmentName = departmentName,
        status = status,
        employeeCount = employeeCount,
        activeEmployeeCount = activeEmployeeCount,
        inactiveEmployeeCount = inactiveEmployeeCount,
        totalSalary = totalSalary,
        averageSalary = averageSalary,
    )