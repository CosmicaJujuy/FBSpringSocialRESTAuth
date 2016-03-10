package com.ozgen.server.security.social.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ozgen.server.entity.User;
import com.ozgen.server.entity.BaseEntity.Status;
import com.ozgen.server.repository.UserRepository;
import com.ozgen.server.security.oauth.RegistrationDTO;

@Service
public class UserService {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

	@Autowired
	private UserRepository repository;

	@Transactional
	public User registerNewUserAccount(RegistrationDTO userAccountData) throws DuplicateEmailException {
		LOGGER.debug("Registering new user account with information: {}", userAccountData);

		if (emailExist(userAccountData.getEmail())) {
			LOGGER.debug("Email: {} exists. Throwing exception.", userAccountData.getEmail());
			throw new DuplicateEmailException(
					"The email address: " + userAccountData.getEmail() + " is already in use.");
		}

		LOGGER.debug("Email: {} does not exist. Continuing registration.", userAccountData.getEmail());

		User registered = User.newEntity();
		registered.setEmail(userAccountData.getEmail());
		registered.setFirstName(userAccountData.getFirstName());
		registered.setLastName(userAccountData.getLastName());
		registered.setPassword(null);
		registered.addRole(User.Role.ROLE_USER_REST_MOBILE);
		registered.setSignInProvider(userAccountData.getSignInProvider());

		LOGGER.debug("Persisting new user with information: {}", registered);

		return repository.save(registered);
	}

	private boolean emailExist(String email) {
		LOGGER.debug("Checking if email {} is already found from the database.", email);

		User user = repository.findByEmailAndStatus(email, Status.ENABLED);

		if (user != null) {
			LOGGER.debug("User account: {} found with email: {}. Returning true.", user, email);
			return true;
		}

		LOGGER.debug("No user account found with email: {}. Returning false.", email);

		return false;
	}

}
