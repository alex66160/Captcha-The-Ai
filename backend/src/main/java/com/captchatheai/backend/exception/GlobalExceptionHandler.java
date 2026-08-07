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
import com.captchatheai.backend.chat.exception.CannotChatException;
import com.captchatheai.backend.chat.exception.ChatCooldownException;
import com.captchatheai.backend.chat.exception.InvalidChatMessageException;
import com.captchatheai.backend.player.exception.GetAiPlayerDeniedException;
import com.captchatheai.backend.player.exception.PlayerDisconnectedException;
import com.captchatheai.backend.player.exception.PlayerNotFoundException;
import com.captchatheai.backend.question.exception.GetQuestionDeniedException;
import com.captchatheai.backend.question.exception.InvalidQuestionException;
import com.captchatheai.backend.question.exception.NotQuestionPhaseException;
import com.captchatheai.backend.question.exception.NotQuestionWriterException;
import com.captchatheai.backend.question.exception.QuestionAlreadyWrittenException;
import com.captchatheai.backend.question.exception.SendQuestionDeniedException;
import com.captchatheai.backend.vote.exception.AlreadyVotedException;
import com.captchatheai.backend.vote.exception.GetTiedPlayersDeniedException;
import com.captchatheai.backend.vote.exception.GetVotesDeniedException;
import com.captchatheai.backend.vote.exception.VotingDeniedException;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler({ GetAnswersDeniedException.class, GetQuestionDeniedException.class,
			GetVotesDeniedException.class, GetTiedPlayersDeniedException.class, PlayerNotFoundException.class,
			GetAiPlayerDeniedException.class })
	public void handleRestExceptions(RuntimeException e) {
		log.warn(e.getMessage());
	}

	@MessageExceptionHandler({ NotAnswerPhaseException.class, SendAnswerDeniedException.class,
			CannotAnswerAsQuestionWriterException.class, AnswerAlreadyWrittenException.class,
			InvalidAnswerException.class, CannotChatException.class, ChatCooldownException.class,
			InvalidChatMessageException.class, NotQuestionPhaseException.class, NotQuestionWriterException.class,
			QuestionAlreadyWrittenException.class, SendQuestionDeniedException.class, InvalidQuestionException.class,
			VotingDeniedException.class, AlreadyVotedException.class, PlayerNotFoundException.class,
			PlayerDisconnectedException.class })
	public void handleStompException(RuntimeException e) {
		log.warn(e.getMessage());
	}

}
