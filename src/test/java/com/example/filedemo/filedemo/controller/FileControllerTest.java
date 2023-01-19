package com.example.filedemo.filedemo.controller;

import com.example.filedemo.filedemo.model.RegisterForm;
import com.example.filedemo.filedemo.model.StringResponse;
import com.example.filedemo.filedemo.service.FileStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    @InjectMocks
    FileController classToTest;

    @Mock
    FileStorageService  fileStorageServiceMock;


    @Test
    void saveAsPdf() {
        when(fileStorageServiceMock.saveAsPdf(any(RegisterForm.class))).thenReturn("Success");
        ResponseEntity<StringResponse> response = classToTest.saveAsPdf(RegisterForm.builder().build());

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getResponse()).isEqualTo("Success");
        /*
        {
            "response": "Success"
        }

         */
    }
}
