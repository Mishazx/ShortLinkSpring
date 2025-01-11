package ru.mishazx.shortlinkspring.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import ru.mishazx.shortlinkspring.service.UrlService.UrlNotFoundException;
import ru.mishazx.shortlinkspring.service.UrlService.UrlExpiredException;
import ru.mishazx.shortlinkspring.service.UrlService.ClickLimitExceededException;

@ControllerAdvice
public class UrlExceptionHandler {

    @ExceptionHandler({
        UrlNotFoundException.class,
        UrlExpiredException.class,
        ClickLimitExceededException.class
    })
    public ModelAndView handleUrlException(RuntimeException ex) {
        ModelAndView mav = new ModelAndView("url/error");
        mav.addObject("errorMessage", ex.getMessage());
        return mav;
    }
} 