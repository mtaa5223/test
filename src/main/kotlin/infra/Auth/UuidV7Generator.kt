package com.example.infra.Auth

import com.fasterxml.uuid.Generators
import com.fasterxml.uuid.impl.TimeBasedEpochGenerator
import java.util.UUID

class UuidV7Generator(
    private val gen: TimeBasedEpochGenerator = Generators.timeBasedEpochGenerator(),
) {
    fun next(): UUID = gen.generate()
}
