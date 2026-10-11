package sanon.test.department_api.configuration


import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import sanon.test.department_api.exception.BadRequestException
import sanon.test.department_api.exception.ConflictException
import sanon.test.department_api.exception.DepartmentMoveFailedException
import sanon.test.department_api.exception.NotFoundException
import sanon.test.department_api.models.response.ErrorResponse
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
class ExceptionHandler {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(this::class.java)
        private val REQUEST_FIELD_ERRORS =
            listOf(
                NotNull::class.simpleName,
                NotBlank::class.simpleName,
                NotEmpty::class.simpleName,
            )
    }

    @ExceptionHandler(RuntimeException::class)
    fun runtimeException(exception: RuntimeException): ResponseEntity<ErrorResponse> {
        LOGGER.error("Runtime exception error : ${exception.javaClass.simpleName} occurred: ${exception.message}", exception)
        return ResponseEntity(
            ErrorResponse("INTERNAL_ERROR", "Unexpected error"),
            HttpStatus.INTERNAL_SERVER_ERROR,
        )
    }

    @ExceptionHandler(BadRequestException::class)
    fun badRequestException(exception: BadRequestException): ResponseEntity<ErrorResponse> {
        LOGGER.error("Bad request exception : ${exception.javaClass.simpleName} occurred: ${exception.message}", exception)
        return ResponseEntity(
            ErrorResponse(exception.code, exception.message ?: ""),
            HttpStatus.BAD_REQUEST,
        )
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun invalidBodyException(exception: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        LOGGER.warn("Invalid request body: ${exception.message}")
        return ResponseEntity(
            ErrorResponse("VALIDATION_ERROR", "Request body is invalid"),
            HttpStatus.BAD_REQUEST,
        )
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun invalidPathException(exception: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponse> {
        LOGGER.warn("Invalid path value: ${exception.name}=${exception.value}")
        return ResponseEntity(
            ErrorResponse("VALIDATION_ERROR", "${exception.name} is invalid"),
            HttpStatus.BAD_REQUEST,
        )
    }

    @ExceptionHandler(NotFoundException::class)
    fun notFoundException(exception: NotFoundException): ResponseEntity<ErrorResponse> {
        LOGGER.error("NotFound exception : ${exception.javaClass.simpleName} occurred: ${exception.message}", exception)
        return ResponseEntity(
            ErrorResponse(exception.code, exception.message ?: ""),
            HttpStatus.NOT_FOUND,
        )
    }

    @ExceptionHandler(ConflictException::class)
    fun conflictException(exception: ConflictException): ResponseEntity<ErrorResponse> {
        LOGGER.error("Conflict exception : ${exception.javaClass.simpleName} occurred: ${exception.message}", exception)
        return ResponseEntity(
            ErrorResponse(exception.code, exception.message ?: ""),
            HttpStatus.CONFLICT,

        )
    }

    @ExceptionHandler(DepartmentMoveFailedException::class)
    fun departmentMoveFailedException(exception: DepartmentMoveFailedException): ResponseEntity<ErrorResponse> {
        LOGGER.error("Department move failed exception : ${exception.javaClass.simpleName} occurred: ${exception.message}", exception)
        return ResponseEntity(
            ErrorResponse(exception.code, exception.message ?: ""),
        HttpStatus.INTERNAL_SERVER_ERROR
        )
    }
}