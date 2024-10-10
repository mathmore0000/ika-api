package ika.config;

import ika.utils.exceptions.ResourceNotFoundException;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Trata exceções de tipo inválido, como quando um UUID não é válido.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        // Para outros tipos de erros de conversão, retorna uma mensagem genérica
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Invalid parameter type: '" + ex.getValue() + "' for parameter '" + ex.getName() + "'.");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<String> handleNoResourceFoundExceptions() {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("No resource found");
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFoundExceptions(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    /**
     * Trata outras exceções gerais que você desejar capturar e tratar globalmente.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllExceptions(Exception ex) {
        // Loga a exceção completa para referência futura
        ex.printStackTrace();

        // Retorna uma resposta com um status 500 (Internal Server Error)
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<String> handleWrongMethodExceptions(Exception ex) {
        return handleNoResourceFoundExceptions();
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<String> handleParameterNotPassedExceptions(MissingServletRequestParameterException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }
}

