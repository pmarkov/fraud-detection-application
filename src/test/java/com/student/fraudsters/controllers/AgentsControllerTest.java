package com.student.fraudsters.controllers;

import com.student.fraudsters.services.AgentsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AgentsControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private AgentsService service;

    @Test
    public void should_get_client_count() {
        when(service.getClientCount()).thenReturn(Map.of("result", 5L));
        assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/agents/client-count/",
                String.class)).isEqualTo("{\"result\":5}");
    }
}
