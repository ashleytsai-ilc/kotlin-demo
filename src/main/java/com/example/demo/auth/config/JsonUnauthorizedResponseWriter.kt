package com.example.demo.auth.config

import com.example.demo.common.ApiErrorCatalog
import com.example.demo.common.ApiErrorKey
import com.example.demo.common.ApiErrorResponse
import com.example.demo.common.CommonErrorDetail
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import java.io.IOException

@Component
class JsonUnauthorizedResponseWriter(
    private val objectMapper: ObjectMapper,
) {
    @Throws(IOException::class)
    fun write(response: HttpServletResponse) {
        response.status = HttpStatus.UNAUTHORIZED.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        val body: ApiErrorResponse =
            ApiErrorCatalog.response(
                ApiErrorKey.UNAUTHORIZED,
                listOf(CommonErrorDetail.VALID_BEARER_TOKEN_REQUIRED.toDetail()),
            )
        objectMapper.writeValue(response.outputStream, body)
    }
}
