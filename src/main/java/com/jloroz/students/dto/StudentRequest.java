package com.jloroz.students.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StudentRequest(
        @NotBlank String name,
        @NotNull @Min(1) @Max(120) Integer age,
        @NotBlank String className) {
}
