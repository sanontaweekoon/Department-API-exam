package sanon.test.department_api.exception

import org.springframework.http.HttpStatus

class ConflictException(code: String, message: String) : ApiException(HttpStatus.CONFLICT, code, message) {
}