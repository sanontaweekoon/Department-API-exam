package sanon.test.department_api.exception

import org.springframework.http.HttpStatus

abstract class ApiException (
    val status: HttpStatus,
    val code: String,
    message: String
) : RuntimeException(message)