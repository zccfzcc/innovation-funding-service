package com.worth.ifs.application.resource;

import java.util.List;

public class ApplicationSummaryPageResource {

	private long totalElements;
	private int totalPages;
	private List<ApplicationSummaryResource> content;
	private int number;
	private int size;

	public long getTotalElements() {
		return totalElements;
	}
	public void setTotalElements(long totalElements) {
		this.totalElements = totalElements;
	}
	public int getTotalPages() {
		return totalPages;
	}
	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}
	public List<ApplicationSummaryResource> getContent() {
		return content;
	}
	public void setContent(List<ApplicationSummaryResource> content) {
		this.content = content;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}

	public boolean hasPrevious() {
		return number > 0;
	}
	
	public boolean hasNext() {
		return totalPages > (number + 1);
	}
}
