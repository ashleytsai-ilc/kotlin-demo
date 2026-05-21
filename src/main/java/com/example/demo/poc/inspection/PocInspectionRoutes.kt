package com.example.demo.poc.inspection;

public final class PocInspectionRoutes {

    public static final String BASE_PATH = "/api/poc";
    public static final String USERS_ROUTE = "/users";
    public static final String REVOKED_TOKENS_ROUTE = "/revoked-tokens";
    public static final String USERS_PATH = BASE_PATH + USERS_ROUTE;
    public static final String REVOKED_TOKENS_PATH = BASE_PATH + REVOKED_TOKENS_ROUTE;

    private PocInspectionRoutes() {
    }
}
