package de.lmoesle.processautomationexample.adapter.in.rest;

import de.lmoesle.processautomationexample.domain.tasklist.TaskNichtGefundenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(TaskNichtGefundenException.class)
    public ProblemDetail behandleNichtGefundeneAufgabe(TaskNichtGefundenException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problemDetail.setTitle("Aufgabe nicht gefunden");
        return problemDetail;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail behandleUngueltigeAnfrage(IllegalArgumentException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problemDetail.setTitle("Ungueltige Anfrage");
        return problemDetail;
    }
}
