package sanon.test.department_api.controller

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/instance")
class InstanceController (@Value("\${app.instance-name}") private val instanceName: String) {
    @GetMapping
    fun findAll(): Map<String, String> = mapOf("instance" to instanceName)

}