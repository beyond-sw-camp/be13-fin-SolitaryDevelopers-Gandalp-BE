package com.gandalp.gandalp.member.domain.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomPage<T> {
	private List<T> content;
	private int totalPages;
	private long totalElements;
}
