package com.student.fraudsters.detection.exception;

import com.student.fraudsters.detection.execution.task.core.Task;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class FraudDetectionTaskFailedException extends RuntimeException {

    public FraudDetectionTaskFailedException(Task task) {
        super(String.format("Task '%s' failed with: %s", task, task.getErrorMessage()));
    }
}
