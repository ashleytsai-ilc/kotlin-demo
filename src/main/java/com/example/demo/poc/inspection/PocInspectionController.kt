package com.example.demo.poc.inspection

import com.example.demo.poc.inspection.dto.PocRevokedTokenInspectionResponse
import com.example.demo.poc.inspection.dto.PocUserInspectionResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(PocInspectionRoutes.BASE_PATH)
class PocInspectionController(
    private val pocInspectionService: PocInspectionService,
) {
    @GetMapping(PocInspectionRoutes.USERS_ROUTE)
    fun users(): List<PocUserInspectionResponse> = pocInspectionService.users()

    @GetMapping(PocInspectionRoutes.REVOKED_TOKENS_ROUTE)
    fun revokedTokens(): List<PocRevokedTokenInspectionResponse> = pocInspectionService.revokedTokens()
}
