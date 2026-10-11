package sanon.test.department_api

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.sql.Timestamp
import kotlin.test.assertEquals

@Import(TestcontainersConfiguration::class)
@SpringBootTest
class EmployeeMoveIntegrationTest @Autowired constructor(
    private val context: WebApplicationContext,
    private val jdbc: JdbcTemplate,
) {
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build()
    }

    private fun move(employeeId: String, body: String) =
        mockMvc.perform(
            put("/api/employees/$employeeId/department")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        )

    private fun departmentIdOf(employeeId: Long): Long =
        jdbc.queryForObject("SELECT department_id FROM employees WHERE id = ?", Long::class.javaObjectType, employeeId)!!

    private fun updatedAtOf(employeeId: Long): Timestamp =
        jdbc.queryForObject("SELECT updated_at FROM employees WHERE id = ?", Timestamp::class.java, employeeId)!!

    @Test
    fun `move active employee to active department returns 200 and updates department`() {
        val before = updatedAtOf(1)

        move("1", """{"departmentCode":"HR"}""")
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.employeeId").value(1))
            .andExpect(jsonPath("$.departmentCode").value("HR"))

        assertEquals(2L, departmentIdOf(1))  // ENG(1) → HR(2)
        assert(updatedAtOf(1).after(before)) // updated_at เปลี่ยน
    }

    @Test
    fun `move to same department is no-op and keeps updated_at`() {
        val before = updatedAtOf(2)

        move("2", """{"departmentCode":"HR"}""")
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.departmentCode").value("HR"))

        assertEquals(before, updatedAtOf(2))                // ไม่มีการ update
    }

    @Test
    fun `invalid departmentCode returns 400 VALIDATION_ERROR`() {
        listOf(
            """{}""",
            """{"departmentCode":null}""",
            """{"departmentCode":123}""",
            """{"departmentCode":""}""",
            """{bad json""",
        ).forEach { body ->
            move("2", body)
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
        }
    }

    @Test
    fun `invalid employeeId returns 400 VALIDATION_ERROR`() {
        listOf("0", "-1", "abc").forEach { id ->
            move(id, """{"departmentCode":"HR"}""")
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
        }
    }

    @Test
    fun `unknown employee returns 404 EMPLOYEE_NOT_FOUND before checking department`() {
        move("10001", """{"departmentCode":"UNKNOWN"}""")
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("EMPLOYEE_NOT_FOUND"))
    }

    @Test
    fun `unknown department returns 404 DEPARTMENT_NOT_FOUND`() {
        move("2", """{"departmentCode":"UNKNOWN"}""")
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("DEPARTMENT_NOT_FOUND"))
    }

    @Test
    fun `inactive employee returns 409 EMPLOYEE_INACTIVE even if target is inactive`() {
        move("7", """{"departmentCode":"LEGACY"}""")
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.code").value("EMPLOYEE_INACTIVE"))
    }

    @Test
    fun `inactive target department returns 409 DEPARTMENT_INACTIVE`() {
        move("2", """{"departmentCode":"LEGACY"}""")
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.code").value("DEPARTMENT_INACTIVE"))
    }

    @Test
    fun `persistence failure rolls back and returns 500 DEPARTMENT_MOVE_FAILED`() {
        val departmentBefore = departmentIdOf(3)
        val updatedAtBefore = updatedAtOf(3)

        // จำลอง DB error จริง: trigger ที่ทำให้ UPDATE ของ employee 3 ล้มเหลว
        jdbc.execute(
            """
            CREATE OR REPLACE FUNCTION fail_employee_update() RETURNS trigger AS $$
            BEGIN
                RAISE EXCEPTION 'simulated persistence failure';
            END;
            $$ LANGUAGE plpgsql
            """
        )
        jdbc.execute(
            """
            CREATE TRIGGER fail_employee_3_update
            BEFORE UPDATE ON employees
            FOR EACH ROW WHEN (OLD.id = 3)
            EXECUTE FUNCTION fail_employee_update()
            """
        )

        try {
            move("3", """{"departmentCode":"HR"}""")
                .andExpect(status().isInternalServerError)
                .andExpect(jsonPath("$.code").value("DEPARTMENT_MOVE_FAILED"))
        } finally {
            // ลบ trigger ทิ้งเสมอ แม้ test จะ fail
            jdbc.execute("DROP TRIGGER IF EXISTS fail_employee_3_update ON employees")
            jdbc.execute("DROP FUNCTION IF EXISTS fail_employee_update()")
        }

        assertEquals(departmentBefore, departmentIdOf(3))
        assertEquals(updatedAtBefore, updatedAtOf(3))
    }
}