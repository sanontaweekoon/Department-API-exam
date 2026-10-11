package sanon.test.department_api.configuration

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID


@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class RequestLoggingFilter : OncePerRequestFilter() {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(RequestLoggingFilter::class.java)
        const val HEADER_NAME = "X-Request-ID"

        private val VALID_ID = Regex("^[A-Za-z0-9._-]{1,64}$")
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean =
        request.requestURI.startsWith("/actuator")

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val incoming = request.getHeader(HEADER_NAME)
        val requestId =
            if (incoming != null && VALID_ID.matches(incoming)) incoming
            else UUID.randomUUID().toString()

        MDC.put("requestId", requestId)
        MDC.put("method", request.method)
        MDC.put("path", request.requestURI)
        response.setHeader(HEADER_NAME, requestId)

        val start = System.nanoTime()
        try {
            filterChain.doFilter(request, response)
        } finally {
            val durationMs = (System.nanoTime() - start) / 1_000_000
            MDC.put("status", response.status.toString())
            MDC.put("durationMs", durationMs.toString())
            LOGGER.info("{} {} {} {}ms", request.method, request.requestURI, response.status, durationMs)
            MDC.clear()
        }
    }
}