package com.atanava.locator.model;

import org.springframework.security.core.GrantedAuthority;
/** @QUESTION: Why did you not set ROLE_* using constructor */
public enum Role implements GrantedAuthority {
	USER,
	ADMIN;

	@Override
	public String getAuthority() {
		//   https://stackoverflow.com/a/19542316/548473
		return "ROLE_" + name();
	}
}
