package com.ozgen.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ozgen.server.entity.User;
import com.ozgen.server.entity.BaseEntity.Status;

public interface UserRepository extends JpaRepository<User, String> {

	public User findByEmailAndStatus(String email, Status status);

	public User findByIdAndStatus(String id, Status status);
}
