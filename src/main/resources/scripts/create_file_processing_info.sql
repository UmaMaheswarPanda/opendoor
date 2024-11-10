CREATE TABLE IF NOT EXISTS opendoor.file_processing_info (
    id SERIAL PRIMARY KEY,
    source_file_name VARCHAR(255),
    remarks TEXT,
    source_file_path VARCHAR(255),
    status VARCHAR(50),
    file_type VARCHAR(50),
    s3_uploaded_status VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);