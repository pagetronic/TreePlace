package com.agroneo.treeplace.api;

public class ApiResponse {
	private int code;
	private Json result;

	public ApiResponse(int code, String result) {
		this.code = code;
		this.result = new Json(result);
	}

	public int getCode() {
		return code;
	}

	public Json getResult() {
		return result;
	}

}
