package sanon.test.department_api

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
	fromApplication<DepartmentApiApplication>().with(TestcontainersConfiguration::class).run(*args)
}
