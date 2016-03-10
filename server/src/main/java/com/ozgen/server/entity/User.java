package com.ozgen.server.entity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Entity
@Table(name = "user")
public class User extends BaseEntity {

	@Column(name = "email", length = 255, nullable = false, unique = true)
	private String email;

	@Column(name = "first_name", length = 255, nullable = false)
	private String firstName;

	@Column(name = "last_name", length = 255, nullable = false)
	private String lastName;

	@Column(name = "password", length = 255)
	private String password;

	@Column(name = "role", length = 255, nullable = false)
	private String rolesString;

	@Enumerated(EnumType.STRING)
	@Column(name = "sign_in_provider", length = 20)
	private SocialMediaService signInProvider;

	public User() {
		super();
	}

	private User(String id, Status status) {
		super(id, status);
	}

	public static User newEntity() {
		return new User(UUID.randomUUID().toString(), Status.ENABLED);
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Set<GrantedAuthority> getAuthorities() {
		String[] roles = rolesString.split(",");
		Set<GrantedAuthority> rols = new HashSet<GrantedAuthority>();
		for (int i = 0; i < roles.length; i++) {
			rols.add(new SimpleGrantedAuthority(roles[i]));
		}
		return rols;
	}

	public void setRolesString(String rolesString) {
		this.rolesString = rolesString;

	}

	public void addRole(Role role) {
		if (this.rolesString == null) {
			this.rolesString = role.toString();
		} else {
			this.rolesString += "," + role.toString();
		}
	}

	public SocialMediaService getSignInProvider() {
		return signInProvider;
	}

	public void setSignInProvider(SocialMediaService signInProvider) {
		this.signInProvider = signInProvider;
	}

	public enum Role {
		ROLE_USER_REST_MOBILE
	}

	public enum SocialMediaService {
		FACEBOOK, TWITTER, LINKEDIN
	}

}
