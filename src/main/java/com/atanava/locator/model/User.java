package com.atanava.locator.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;
import org.springframework.data.domain.Persistable;
import org.springframework.data.util.ProxyUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.Set;

import com.atanava.locator.utils.JsonDeserializers;

@Entity
@Table(name = "users")
@Access(AccessType.FIELD)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class User implements Persistable<Integer> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	protected Integer id;

	// doesn't work for hibernate lazy proxy
	public int id() {
		Assert.notNull(id, "Entity must have id");
		return id;
	}

	@Column(name = "email", nullable = false, unique = true)
	@Email
	@NotEmpty
	@Size(max = 128)
	private String email;

	@Column(name = "first_name")
	@Size(max = 128)
	private String firstName;

	@Column(name = "last_name")
	@Size(max = 128)
	private String lastName;

	@Column(name = "password")
	@Size(max = 256)
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@JsonDeserialize(using = JsonDeserializers.PasswordDeserializer.class)
	private String password;

	@Enumerated(EnumType.STRING)
	@CollectionTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"), uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "role"}, name = "user_roles_unique")})
	@Column(name = "role")
	@ElementCollection(fetch = FetchType.EAGER)
	private Set<Role> roles;

	public void setEmail(String email) {
		this.email = StringUtils.hasText(email) ? email.toLowerCase() : null;
	}

	@JsonIgnore
	@Override
	public boolean isNew() {
		return id == null;
	}

	//    https://stackoverflow.com/questions/1638723
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || !getClass().equals(ProxyUtils.getUserClass(o))) {
			return false;
		}
		User that = (User) o;
		return id != null && id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return id == null ? 0 : id;
	}
}
