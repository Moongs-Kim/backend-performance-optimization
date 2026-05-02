CREATE TABLE IF NOT EXISTS upload_file (
	upload_file_id BIGINT NOT NULL AUTO_INCREMENT,
    upload_file_name VARCHAR(100) NOT NULL,
    storage_key VARCHAR(200) NOT NULL,
    content_type VARCHAR(30) NOT NULL,
    size BIGINT DEFAULT 0,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (upload_file_id)
);

CREATE TABLE IF NOT EXISTS member (
	member_id BIGINT NOT NULL AUTO_INCREMENT,
    login_id VARCHAR(30) NOT NULL,
    password varchar(30) NOT NULL,
    name VARCHAR(30) NOT NULL,
	birth_date DATE NOT NULL,
    gender VARCHAR(10) NOT NULL,
    email VARCHAR(50) NOT NULL,
    address VARCHAR(100) NOT NULL,
    upload_file_id BIGINT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (member_id),
    UNIQUE KEY uq_login_id (login_id),
    CONSTRAINT fk_member_upload_file FOREIGN KEY (upload_file_id)
		REFERENCES upload_file(upload_file_id)
);

CREATE TABLE IF NOT EXISTS friend (
	friend_id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    friend_member_id BIGINT NOT NULL,
    request_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (friend_id),
    CONSTRAINT uq_member_id_friend_member_id UNIQUE (member_id, friend_member_id),
    CONSTRAINT ck_member_id_friend_member_id CHECK (member_id < friend_member_id),
    CONSTRAINT ck_request_id CHECK (request_id IN (member_id, friend_member_id)),
    CONSTRAINT fk_friend_member FOREIGN KEY (member_id)
		REFERENCES member(member_id),
    CONSTRAINT fk_friend_friend_member FOREIGN KEY (friend_member_id)
        REFERENCES member(member_id),
    CONSTRAINT fk_friend_request FOREIGN KEY (request_id)
        REFERENCES member(member_id)
);

CREATE TABLE IF NOT EXISTS category (
	category_id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(20) NOT NULL,
	created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

	PRIMARY KEY (category_id)
);

CREATE TABLE IF NOT EXISTS board (
	board_id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    title VARCHAR(50) NOT NULL,
    content VARCHAR(5000) NOT NULL,
    board_open VARCHAR(20) NOT NULL,
    category_id BIGINT NOT NULL,
    view_count INT DEFAULT 0 NOT NULL,
    deleted_at TIMESTAMP,
    version BIGINT DEFAULT 0 NOT NULL,
	created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (board_id),

	CONSTRAINT fk_board_member FOREIGN KEY (member_id)
		REFERENCES member(member_id),
	CONSTRAINT fk_board_category FOREIGN KEY (category_id)
		REFERENCES category(category_id)
);


CREATE TABLE IF NOT EXISTS board_attach_file (
	board_attach_file_id BIGINT NOT NULL AUTO_INCREMENT,
	board_id BIGINT NOT NULL,
    upload_file_id BIGINT NOT NULL,
    deleted_at TIMESTAMP,
	created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

	PRIMARY KEY (board_attach_file_id),
    CONSTRAINT uq_board_upload_file UNIQUE (board_id, upload_file_id),
    CONSTRAINT fk_board_attach_file_board FOREIGN KEY (board_id)
		REFERENCES board(board_id),
	CONSTRAINT fk_board_attach_file_upload_file FOREIGN KEY (upload_file_id)
		REFERENCES upload_file(upload_file_id)
);

CREATE TABLE IF NOT EXISTS comment (
	comment_id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    board_id BIGINT NOT NULL,
    comment_content VARCHAR(500) NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (comment_id),
    CONSTRAINT fk_comment_member FOREIGN KEY (member_id)
		REFERENCES member (member_id),
	CONSTRAINT fk_comment_board FOREIGN KEY (board_id)
		REFERENCES board (board_id)
);

CREATE TABLE IF NOT EXISTS reply (
	reply_id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    comment_id BIGINT NOT NULL,
    reply_content VARCHAR(500) NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (reply_id),
    CONSTRAINT fk_reply_member FOREIGN KEY (member_id)
		REFERENCES member (member_id),
	CONSTRAINT fk_reply_comment FOREIGN KEY (comment_id)
		REFERENCES comment (comment_id)
);

CREATE TABLE IF NOT EXISTS board_view_history (
	board_view_history_id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    board_id BIGINT NOT NULL,
    view_at TIMESTAMP NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (board_view_history_id),
    CONSTRAINT uq_board_view_history UNIQUE (member_id, board_id),
    CONSTRAINT fk_board_view_member FOREIGN KEY (member_id)
		REFERENCES member (member_id),
	CONSTRAINT fk_board_view_board FOREIGN KEY (board_id)
		REFERENCES board (board_id)
);


CREATE TABLE IF NOT EXISTS likes (
	like_id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
	board_id BIGINT NOT NULL,
	created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (like_id),
    CONSTRAINT uq_board_member UNIQUE (board_id, member_id),
    CONSTRAINT fk_likes_member FOREIGN KEY (member_id)
		REFERENCES member(member_id),
	CONSTRAINT fk_likes_board FOREIGN KEY (board_id)
		REFERENCES board(board_id)
);

CREATE TABLE IF NOT EXISTS shedlock (
	name VARCHAR(100) NOT NULL,
    lock_until TIMESTAMP NOT NULL,
    locked_at TIMESTAMP NOT NULL,
    locked_by VARCHAR(255) NOT NULL,
    primary key (name)
);

CREATE TABLE IF NOT EXISTS spring_session (
    primary_id CHAR(36) NOT NULL,
    session_id CHAR(36) NOT NULL,
    creation_time BIGINT NOT NULL,
    last_access_time BIGINT NOT NULL,
    max_inactive_interval INT NOT NULL,
    expiry_time BIGINT NOT NULL,
    principal_name VARCHAR(100),

    PRIMARY KEY (primary_id),
    CONSTRAINT idx_spring_session_id UNIQUE (session_id)
);

CREATE INDEX IF NOT EXISTS idx_spring_session_expiry_time ON spring_session (expiry_time);
CREATE INDEX IF NOT EXISTS idx_spring_session_principal_name ON spring_session (principal_name);

CREATE TABLE IF NOT EXISTS spring_session_attributes (
    session_primary_id CHAR(36) NOT NULL,
    attribute_name VARCHAR(200) NOT NULL,
    attribute_bytes BLOB NOT NULL,

    PRIMARY KEY (session_primary_id, attribute_name),
    CONSTRAINT fk_spring_session_attributes_spring_session FOREIGN KEY (session_primary_id)
        REFERENCES spring_session(primary_id) ON DELETE CASCADE
);