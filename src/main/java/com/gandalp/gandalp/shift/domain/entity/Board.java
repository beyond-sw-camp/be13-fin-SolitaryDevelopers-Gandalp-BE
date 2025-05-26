package com.gandalp.gandalp.shift.domain.entity;

import com.gandalp.gandalp.common.entity.BaseEntity;
import com.gandalp.gandalp.hospital.domain.entity.Department;
import com.gandalp.gandalp.member.domain.entity.Member;
import com.gandalp.gandalp.member.domain.entity.Nurse;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Board extends BaseEntity{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "board-id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member-id")
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "department-id")
	private Department department;

	@Column(nullable = false, length = 200)
	private String content;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private BoardStatus boardStatus;

	@OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Comment> comments = new ArrayList<>();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "nurse-id")
	private Nurse nurse;


	// 채택할 때 boardstatus 값 변경
	public void completeRequest() {
		this.boardStatus = BoardStatus.Completed;
	}




//	@Builder
//	public Board(ShiftUpdateDto shiftUpdateDto, Member member, Department department) {
//		this.content = shiftUpdateDto.getContent();
//		this.member = member;
//		this.department = department;
//
//	}


//	public void update(String content) {
//		this.content = content;
//	}




}
