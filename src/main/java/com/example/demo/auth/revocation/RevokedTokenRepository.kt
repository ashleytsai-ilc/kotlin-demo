package com.example.demo.auth.revocation

import org.springframework.data.jpa.repository.JpaRepository

interface RevokedTokenRepository : JpaRepository<RevokedToken, String>
