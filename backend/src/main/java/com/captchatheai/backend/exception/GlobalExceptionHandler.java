package com.captchatheai.backend.exception;

import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.captchatheai.backend.answer.exception.AnswerAlreadyWrittenException;
import com.captchatheai.backend.answer.exception.CannotAnswerAsQuestionWriterException;
import com.captchatheai.backend.answer.exception.GetAnswersDeniedException;
import com.captchatheai.backend.answer.exception.InvalidAnswerException;
import com.captchatheai.backend.answer.exception.NotAnswerPhaseException;
import com.captchatheai.backend.answer.exception.SendAnswerDeniedException;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler({ GetAnswersDeniedException.class })
	public void handleRestExceptions(RuntimeException e) {
		log.warn(e.getMessage());
	}

	@MessageExceptionHandler({ NotAnswerPhaseException.class, SendAnswerDeniedException.class,
			CannotAnswerAsQuestionWriterException.class, AnswerAlreadyWrittenException.class,
			InvalidAnswerException.class })
	public void handleStompException(RuntimeException e) {
		log.warn(e.getMessage());
	}

}
