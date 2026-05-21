package com.example.demo.user.account;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class UlidGeneratorTest {

    private static final int GENERATED_ID_COUNT = 10;
    private static final String ULID_PATTERN = "[0-9A-HJKMNP-TV-Z]{26}";

    @Test
    void generatesMonotonicallyOrderedUlids() {
        UlidGenerator generator = new UlidGenerator();

        List<String> ids = IntStream.range(0, GENERATED_ID_COUNT)
                .mapToObj(index -> generator.next())
                .toList();

        assertThat(ids)
                .allMatch(id -> id.matches(ULID_PATTERN))
                .doesNotHaveDuplicates()
                .isSorted();
    }
}
