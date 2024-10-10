package ika.config;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import ika.utils.exceptions.ResourceNotFoundException;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Optional;

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

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<String> handleMediaTypeNotSupportedExceptions(HttpMediaTypeNotSupportedException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Media type not supported.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleMissingParameterInBodyExceptions(MethodArgumentNotValidException ex) {
        StringBuilder errorMessage = new StringBuilder("Validation failed for: ");

        // Loop through each field error in the exception
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errorMessage.append("[")
                    .append(fieldError.getDefaultMessage())  // The default validation message
                    .append("], ");
        }

        // Remove the last comma and space
        if (errorMessage.length() > 0) {
            errorMessage.setLength(errorMessage.length() - 2);
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorMessage.toString());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        String errorMessage = "Invalid input format.";

        // Extract detailed information if available
        Optional<String> detailedMessage = getDetailedErrorMessage(ex);
        if (detailedMessage.isPresent()) {
            errorMessage += " " + detailedMessage.get();
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    }

    private Optional<String> getDetailedErrorMessage(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();

        if (cause instanceof InvalidFormatException) {
            InvalidFormatException invalidFormatException = (InvalidFormatException) cause;

            // Extract field name and expected format
            String fieldName = getFieldName(invalidFormatException);
            String targetType = invalidFormatException.getTargetType().getSimpleName();
            String invalidValue = invalidFormatException.getValue().toString();

            // Customize the error message
            String message = String.format(
                    "Invalid input format for field '%s'. Provided value: '%s'. Expected type: '%s'.",
                    fieldName, invalidValue, targetType
            );
            return Optional.of(message);
        }

        return Optional.empty();
    }

    private String getFieldName(InvalidFormatException ex) {
        // Get the path of the problematic field in the JSON
        StringBuilder path = new StringBuilder();
        for (JsonMappingException.Reference reference : ex.getPath()) {
            path.append(reference.getFieldName()).append(".");
        }
        return path.length() > 0 ? path.substring(0, path.length() - 1) : "unknown field";
    }
}

