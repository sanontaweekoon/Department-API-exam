package sanon.test.department_api.exception

import org.springframework.http.HttpStatus

class BadRequestException(message: String, code: String = "VALIDATION_ERROR") : ApiException(HttpStatus.BAD_REQUEST, code, message) {
}