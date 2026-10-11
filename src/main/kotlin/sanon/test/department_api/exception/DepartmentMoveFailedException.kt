package sanon.test.department_api.exception

import org.springframework.http.HttpStatus

class DepartmentMoveFailedException(cause: Throwable) : ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "DEPARTMENT_MOVE_FAILED", "Failed to move employee") {
    init {
        initCause(cause)
    }
}