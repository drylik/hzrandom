package ru.novikov.random.server.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import ru.novikov.random.server.service.RestService;

@Slf4j
@EnableAsync
@RestController
public class MainController {
    private final RestService restService;

    @Autowired
    public MainController(RestService restService) {
        this.restService = restService;
    }

    @RequestMapping(value = "/generate/string", method = RequestMethod.GET)
    public DeferredResult<String> generateString(@RequestParam(value = "s", required = false) String seed) {
        log.info("init: " + Thread.currentThread().getId());
        log.info(seed);
        DeferredResult<String> result = createResult();
        restService.generateRequest(String.class, seed, result);
        return result;
    }

    private <T> DeferredResult<T> createResult() {
        DeferredResult result = new DeferredResult<>(30000L, "TIMEOUT");
        result.onCompletion(() -> log.info("completion" + Thread.currentThread().getId()));
        result.setResultHandler(o -> {
            log.info("o: " + o);
            log.info("result" + Thread.currentThread().getId());
        });
        return result;
    }
}
