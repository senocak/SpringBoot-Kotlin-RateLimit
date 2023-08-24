package com.github.senocak.controller

import com.github.senocak.exception.ServerException
import com.github.senocak.util.OmaErrorMessageType
import org.springframework.http.HttpStatus
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.annotation.CrossOrigin
import java.util.stream.Collectors

@CrossOrigin(origins = ["*"], maxAge = 3600)
abstract class BaseController {
    fun validate(resultOfValidation: BindingResult) {
        if (resultOfValidation.hasErrors())
            throw ServerException(omaErrorMessageType = OmaErrorMessageType.JSON_SCHEMA_VALIDATOR,
                variables = resultOfValidation.fieldErrors.stream()
                    .map { fieldError: FieldError? -> "${fieldError?.field}: ${fieldError?.defaultMessage}" }
                    .collect(Collectors.toList()).toTypedArray())
    }
}
