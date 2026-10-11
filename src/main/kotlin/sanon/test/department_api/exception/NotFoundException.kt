package sanon.test.department_api.exception

import org.springframework.http.HttpStatus

class NotFoundException(code: String, message: String) : ApiException(HttpStatus.NOT_FOUND, code, message) {
}