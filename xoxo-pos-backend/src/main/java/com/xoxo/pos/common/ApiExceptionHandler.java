package com.xoxo.pos.common;
import org.springframework.http.*;import org.springframework.web.bind.annotation.*;import java.time.LocalDateTime;import java.util.Map;
@RestControllerAdvice public class ApiExceptionHandler{
@ExceptionHandler(RuntimeException.class) public ResponseEntity<Map<String,Object>> handle(RuntimeException ex){return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("timestamp",LocalDateTime.now().toString(),"message",ex.getMessage()));}}
