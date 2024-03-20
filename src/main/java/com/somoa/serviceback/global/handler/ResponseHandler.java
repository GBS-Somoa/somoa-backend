package com.somoa.serviceback.global.handler;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Getter
public class ResponseHandler<T>{
    private final T data;
    private final String message;
    private final long timestamp;
    private final int status;
    ResponseHandler(T data, String message, int status) {
        this.data = data;
        this.message = message;
        this.status = status;
        this.timestamp = System.currentTimeMillis();
    }

    public ResponseHandler(String message, int value) {
        this.data = (T) "";
        this.message = message;
        this.status = value;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * public static <T> Mono<ResponseHandler<T>> ok(T data, String message) {
     * String effectiveMessage = (message == null || message.isEmpty()) ? "Success" : message;
     * return Mono.just(new ResponseHandler<>(data, effectiveMessage, 200));
     * }
     * <p>
     * public static <T> Mono<ResponseHandler<T>> error(String message, HttpStatus status) {
     * return Mono.just(new ResponseHandler<>(null, message, status.value()));
     * }
     */
    public static Mono<ResponseEntity<ResponseHandler>> ok(Object data, String message ) {
        ResponseHandler response = new ResponseHandler(data, message,200);
        return Mono.just(ResponseEntity.status(200).body(response));
    }

    // 에러 응답 생성
    public static Mono<ResponseEntity<ResponseHandler>> error(String message, HttpStatus status) {
        ResponseHandler response = new ResponseHandler(null, message, status.value());
        return Mono.just(ResponseEntity.status(status).body(response));
    }

}
