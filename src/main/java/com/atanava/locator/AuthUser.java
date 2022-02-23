/** @QUESTION: Why are you using this package? */
package com.atanava.locator;

import com.atanava.locator.model.User;
import lombok.Getter;
import lombok.ToString;
import org.springframework.lang.NonNull;

@Getter
@ToString(of = "user")
public class AuthUser extends org.springframework.security.core.userdetails.User {

	private final User user;

	public AuthUser(@NonNull User user) {
		super(user.getEmail(), user.getPassword(), user.getRoles());
		this.user = user;
	}

	public int id() {
		return user.id();
	}
}
