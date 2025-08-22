package com.denidove.Logistics.exceptions;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;

@Component
public class RestTemplateResponseErrorHandler implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse httpResponse) throws IOException {
        return httpResponse.getStatusCode().is5xxServerError() ||
                httpResponse.getStatusCode().is4xxClientError();
    }

    @Override
    public void handleError(URI url, HttpMethod method, ClientHttpResponse httpResponse) throws IOException {
        if (httpResponse.getStatusCode().is5xxServerError()) {
            //Обрабатываем SERVER_ERROR
            throw new HttpServerErrorException(httpResponse.getStatusCode());
        } else if (httpResponse.getStatusCode().is4xxClientError()) {
            //Обрабатываем CLIENT_ERROR
            if (httpResponse.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new HttpClientErrorException(httpResponse.getStatusCode(), httpResponse.getStatusText(), httpResponse.getBody().readAllBytes(), Charset.defaultCharset());
            }
        }
    }
}
