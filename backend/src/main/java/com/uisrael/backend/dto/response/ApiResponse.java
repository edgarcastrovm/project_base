package com.uisrael.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Envoltorio estandarizado de respuesta para todos los endpoints de la API.
 * { code, msg, data, error }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private int code;
    private String msg;
    private T data;
    private String error;

    public static <T> ApiResponse<T> ok(String msg, T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .msg(msg)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> created(String msg, T data) {
        return ApiResponse.<T>builder()
                .code(201)
                .msg(msg)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(int code, String msg, String error) {
        return ApiResponse.<T>builder()
                .code(code)
                .msg(msg)
                .error(error)
                .build();
    }
}
