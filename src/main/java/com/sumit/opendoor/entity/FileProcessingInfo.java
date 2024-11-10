package com.sumit.opendoor.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(name="file_processing_info",schema = "opendoor")
public class FileProcessingInfo extends BaseEntity implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY) 
	@Column(name = "id")
	private int id;
	
	@Column(name="source_file_name")
	private String sourceFileName;

	@Column(name="remarks")
	private String remarks;

	@Column(name="source_file_path")
	private String sourceFilePath;

	private String status;
	
	@Column(name="file_type")
	private String fileType;
	
	@Column(name="s3_uploaded_status")
	private String s3UploadedStatus;

}