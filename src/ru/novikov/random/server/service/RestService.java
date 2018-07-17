package ru.novikov.random.server.service;

import org.springframework.web.context.request.async.DeferredResult;

public interface RestService {
    <T> void generateRequest(Class<T> clazz, String clientSeed, DeferredResult<T> result);
}
