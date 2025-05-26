package com.gandalp.gandalp.shift.domain.entity;

import com.gandalp.gandalp.common.entity.BaseEntity;
import com.gandalp.gandalp.member.domain.entity.Member;
import com.gandalp.gandalp.member.domain.entity.Nurse;
import com.gandalp.gandalp.shift.domain.dto.CommentUpdateDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Comment extends BaseEntity {


	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "comment-id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member-id")
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "board-id")
	private Board board;

	@Column(nullable = false, length = 200)
	private String content;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "nurse-id")
	private Nurse nurse;

	@Builder
	public Comment(CommentUpdateDto commentUpdateDto, Member member, Board board) {
		this.content = commentUpdateDto.getContent();
		this.board = board;
		this.member = member;

	}

	public void update(CommentUpdateDto commentUpdateDto) {
		this.content = commentUpdateDto.getContent();
	}

}
