package sanon.test.department_api.controller

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class InstanceHeaderFilter (
    @Value("\${app.instance-name}") private val instanceName: String,
) : OncePerRequestFilter() {
    companion object {
        const val HEADER_NAME = "X-Instance-Name"
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean =
        !request.requestURI.startsWith("/api/")

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        response.setHeader(HEADER_NAME, instanceName)
        filterChain.doFilter(request, response)
    }
}