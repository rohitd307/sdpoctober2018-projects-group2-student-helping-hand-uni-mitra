package com.unimitra.service.impl;

import java.sql.Timestamp;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.unimitra.dao.CategoryDao;
import com.unimitra.dao.DiscussionDao;
import com.unimitra.dao.UserDetailsDao;
import com.unimitra.entity.AnswersEntity;
import com.unimitra.entity.QuestionsEntity;
import com.unimitra.exception.ErrorCodes;
import com.unimitra.exception.UnimitraException;
import com.unimitra.model.AnswerModel;
import com.unimitra.model.DiscussionModel;
import com.unimitra.service.DiscussionService;

@Service
@Transactional
public class DiscussionServiceImpl implements DiscussionService {
	@Autowired
	DiscussionDao discussionDao;
	@Autowired
	CategoryDao categoryDao;
	@Autowired
	UserDetailsDao userDetailsDao;

	@Override
	public ResponseEntity<String> postQuestion(DiscussionModel discussionModel) throws UnimitraException {
		QuestionsEntity questionEntity = new QuestionsEntity();
		questionEntity.setQuestionCategoryId(categoryDao.getCategoryIdFromCategoryName(discussionModel.getCategory()));
		questionEntity.setQuestionCreationDateTime(new Timestamp(System.currentTimeMillis()));
		questionEntity.setQuestionDescription(discussionModel.getQuestion().trim());
		questionEntity.setQuestionActive(true);
		questionEntity.setDiscussionThreadActive(true);
		questionEntity.setQuestionPostedByUserId(discussionModel.getUserId());
		discussionDao.postQuestions(questionEntity);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<String> answerQuestion(AnswerModel answerModel) throws UnimitraException {
		AnswersEntity answersEntity = new AnswersEntity();
		answersEntity.setAnswerDescription(answerModel.getAnswer().trim());
		answersEntity.setAnswerIsActive(true);
		answersEntity.setQuestionId(answerModel.getQuestionId());
		answersEntity.setAnswerPostedByUserId(answerModel.getUserId());
		answersEntity.setAnswerDateTime(new Timestamp(System.currentTimeMillis()));
		answersEntity.setAnswerPostedByUserId(answerModel.getUserId());
		checkIfPostAnswerIsPossible(answerModel.getQuestionId());
		discussionDao.postAnswers(answersEntity);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<String> delete(Integer questionId, Integer answerId, Integer userId)
			throws UnimitraException {
		validateDeleteDiscussionRequest(questionId, answerId);
		if (!(ObjectUtils.isEmpty(questionId))) {
			deleteQuestionIfAddedByUser(questionId, userId);
		} else {
			deleteAnswerIfAddedByUser(answerId, userId);
		}
		return new ResponseEntity<>(HttpStatus.ACCEPTED);
	}

	@Override
	public ResponseEntity<String> closeDiscussionThread(DiscussionModel discussionModel) throws UnimitraException {
		int userId = discussionModel.getUserId();
		boolean isDiscussionThreadActive = discussionModel.isDiscussionThreadActive();
		int questionId = discussionModel.getQuestionId();
		if (isUserStaff(userId)) {
			discussionDao.closeQuestionThread(questionId, isDiscussionThreadActive);
		}
		return new ResponseEntity<>(HttpStatus.ACCEPTED);
	}

	private boolean isUserStaff(int userId) throws UnimitraException {
		return discussionDao.getUserType(userId).equals("Staff");
	}

	private void deleteAnswerIfAddedByUser(Integer answerId, Integer userId) throws UnimitraException {
		if (discussionDao.getAnswerPosterUserId(answerId) == userId) {
			discussionDao.deleteAnswer(answerId);
		} else {
			throw new UnimitraException(ErrorCodes.USER_HAS_NO_ACCESS);
		}
	}

	private void deleteQuestionIfAddedByUser(Integer questionId, Integer userId) throws UnimitraException {
		if (discussionDao.getQuestionPosterUserId(questionId) == userId) {
			discussionDao.deleteQuestion(questionId);
			discussionDao.deletAllAnswersOfQuestion(questionId);
		} else {
			throw new UnimitraException(ErrorCodes.USER_HAS_NO_ACCESS);
		}
	}

	private void validateDeleteDiscussionRequest(Integer questionId, Integer answerId) throws UnimitraException {
		if ((ObjectUtils.isEmpty(questionId)) && (ObjectUtils.isEmpty(answerId))) {
			throw new UnimitraException(ErrorCodes.INVALID_DELETE_DISCUSSION_REQUEST);
		}
		if (!(ObjectUtils.isEmpty(questionId)) && !(ObjectUtils.isEmpty(answerId))) {
			throw new UnimitraException(ErrorCodes.INVALID_DELETE_DISCUSSION_REQUEST);
		}
	}

	private void checkIfPostAnswerIsPossible(int questionId) throws UnimitraException {
		if (!discussionDao.getStatusOfDiscussionThread(questionId)) {
			throw new UnimitraException(ErrorCodes.QUESTION_THREAD_INACTIVE);
		}
		if (!discussionDao.getStatusOfQuestionDeletion(questionId)) {
			throw new UnimitraException(ErrorCodes.QUESTION_NOT_PRESENT);
		}
	}

}
