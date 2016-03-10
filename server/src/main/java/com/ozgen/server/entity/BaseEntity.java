package com.ozgen.server.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;

@MappedSuperclass
public abstract class BaseEntity {

	@Id
	@Column(name = "id", length = 50)
	private String id;

	@Temporal(value = javax.persistence.TemporalType.TIMESTAMP)
	@Column(name = "creation_time", nullable = false)
	private Date creationTime;

	@Temporal(value = javax.persistence.TemporalType.TIMESTAMP)
	@Column(name = "modification_time", nullable = false)
	private Date modificationTime;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", length = 20, nullable = false)
	private Status status;

	public BaseEntity() {
	}

	public BaseEntity(String id, Status status) {
		this.id = id;
		this.status = status;
	}

	public void disable() {
		status = Status.DISABLED;
	}

	public void enable() {
		status = Status.ENABLED;
	}

	@PrePersist
	public void prePersist() {
		Date now = new Date();
		this.creationTime = now;
		this.modificationTime = now;
	}

	@PreUpdate
	public void preUpdate() {
		this.modificationTime = new Date();
	}

	public String getId() {
		return id;
	}

	public Date getCreationTime() {
		return creationTime;
	}

	public Date getModificationTime() {
		return modificationTime;
	}

	public Status getStatus() {
		return status;
	}

	public enum Status {
		ENABLED("1"), DISABLED("0");

		private final String id;

		Status(String id) {
			this.id = id;
		}

		public String getValue() {
			return id;
		}

		public String toString() {
			return id;
		}

	}

}
