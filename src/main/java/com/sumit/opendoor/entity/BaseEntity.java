package com.sumit.opendoor.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.util.Date;

@MappedSuperclass
@Data
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8424138132877303027L;
	public interface Columns {
		String CREATED_AT = "created_at";
		String UPDATED_AT = "updated_at";
		String CREATED_BY = "created_by";
		String UPDATED_BY = "updated_by";
		String DATE_FORMAT = "dd-MM-yyyy HH:mm:ss";
		String TIMEZONE = "Asia/Kolkata";
	}

	@Column(name = Columns.CREATED_AT, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	@CreationTimestamp
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Columns.DATE_FORMAT,timezone = Columns.TIMEZONE)
	private Date createdAt;

	
	@Column(name = Columns.UPDATED_AT)
	@Temporal(TemporalType.TIMESTAMP)
	@UpdateTimestamp
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Columns.DATE_FORMAT,timezone = Columns.TIMEZONE)
	private Date updatedAt;

	@Column(name = Columns.CREATED_BY)
	@CreatedBy
	private String createdBy;

	@Column(name = Columns.UPDATED_BY)
	@LastModifiedBy
	private String updatedBy;

	@PrePersist
	protected void onCreate() {
		createdAt = updatedAt = new Date();
	}
	@PreUpdate
	protected void onUpdate() {
		updatedAt = new Date();
	}
	@PostUpdate
	protected void postUpdate() {
		updatedAt = new Date();
	}
	@PostPersist
	protected void postCreate() {
		createdAt = new Date();
	}
}
