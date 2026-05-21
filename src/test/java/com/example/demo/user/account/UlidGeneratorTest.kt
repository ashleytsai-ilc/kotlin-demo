package com.example.demo.user.account

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class UlidGeneratorTest {
    @Test
    fun generatesMonotonicallyOrderedUlids() {
        val generator = UlidGenerator()

        val ids = List(GENERATED_ID_COUNT) {
            generator.next()
        }

        assertThat(ids)
            .allMatch { id -> ULID_PATTERN.matches(id) }
            .doesNotHaveDuplicates()
            .isSorted()
    }

    companion object {
        private const val GENERATED_ID_COUNT = 10
        private val ULID_PATTERN = Regex("[0-9A-HJKMNP-TV-Z]{26}")
    }
}
