package com.unimitra.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name = "answers")
public class AnswersEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "answer_id")
	private int answerId;
	
	@Column(name = "answer_posted_by_user_id")
	private String answerPostedByUserId;
	
	@Column(name = "answer_descrption")
	private String answerDescription;
	
	@Column(name = "question_id")
	private int questionId;
	
	@Column(name = "answer_date_time")
	private Date answerDateTime;
	
	@Column(name = "answer_is_active")
	private String answerIsActive;
		
}
