package com.example.demo.user.account;

import com.github.f4b6a3.ulid.UlidCreator;
import org.springframework.stereotype.Component;

@Component
public class UlidGenerator {

    public String next() {
        return UlidCreator.getMonotonicUlid().toString();
    }
}
