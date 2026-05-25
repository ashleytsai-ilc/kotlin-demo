package com.example.demo.common

import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

class GlobalExceptionHandlerWebMvcTest {
    private val mockMvc: MockMvc =
        MockMvcBuilders
            .standaloneSetup(GlobalExceptionHandlerTestController())
            .setControllerAdvice(GlobalExceptionHandler())
            .build()

    @Test
    fun mapsApiExceptionToStandardErrorResponse() {
        mockMvc.perform(get(API_EXCEPTION_PATH))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(ApiErrorCatalog.code(ApiErrorKey.REGISTRATION_VALIDATION)))
            .andExpect(jsonPath("$.message").value(ApiErrorCatalog.message(ApiErrorKey.REGISTRATION_VALIDATION)))
            .andExpect(jsonPath("$.details[0].field").value(ErrorField.USERNAME.value()))
            .andExpect(jsonPath("$.details[0].message").value(TEST_DETAIL_MESSAGE))
    }

    @Test
    fun mapsUnreadableJsonToInvalidJsonResponse() {
        mockMvc.perform(
            post(BODY_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"),
        )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(ApiErrorCatalog.code(ApiErrorKey.INVALID_JSON)))
            .andExpect(jsonPath("$.message").value(ApiErrorCatalog.message(ApiErrorKey.INVALID_JSON)))
            .andExpect(jsonPath("$.details[0].field").value(ErrorField.BODY.value()))
    }

    companion object {
        const val API_EXCEPTION_PATH = "/test/api-exception"
        const val BODY_PATH = "/test/body"
        const val TEST_DETAIL_MESSAGE = "Test validation message."
    }

    @RestController
    private class GlobalExceptionHandlerTestController {
        @GetMapping(GlobalExceptionHandlerWebMvcTest.API_EXCEPTION_PATH)
        fun apiException(): Nothing =
            throw ApiErrorCatalog.exception(
                HttpStatus.BAD_REQUEST,
                ApiErrorKey.REGISTRATION_VALIDATION,
                listOf(ErrorDetail(ErrorField.USERNAME, GlobalExceptionHandlerWebMvcTest.TEST_DETAIL_MESSAGE)),
            )

        @PostMapping(GlobalExceptionHandlerWebMvcTest.BODY_PATH)
        fun body(
            @RequestBody request: Map<String, Any?>,
        ): Map<String, Any?> = request
    }
}
