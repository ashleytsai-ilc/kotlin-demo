package com.example.demo.poc.inspection;

import com.example.demo.poc.inspection.dto.PocRevokedTokenInspectionResponse;
import com.example.demo.poc.inspection.dto.PocUserInspectionResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(PocInspectionRoutes.BASE_PATH)
public class PocInspectionController {

    private final PocInspectionService pocInspectionService;

    public PocInspectionController(PocInspectionService pocInspectionService) {
        this.pocInspectionService = pocInspectionService;
    }

    @GetMapping(PocInspectionRoutes.USERS_ROUTE)
    List<PocUserInspectionResponse> users() {
        return pocInspectionService.users();
    }

    @GetMapping(PocInspectionRoutes.REVOKED_TOKENS_ROUTE)
    List<PocRevokedTokenInspectionResponse> revokedTokens() {
        return pocInspectionService.revokedTokens();
    }
}
