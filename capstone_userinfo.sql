CREATE DATABASE IF NOT EXISTS capstone DEFAULT CHARACTER SET utf8mb4;

USE capstone;

CREATE TABLE IF NOT EXISTS user_info (
    orderNum INT AUTO_INCREMENT PRIMARY KEY,
    id VARCHAR(50) NOT NULL UNIQUE,              -- 로그인 ID
    password VARCHAR(100) NOT NULL,              -- 암호화된 비밀번호
    name VARCHAR(50) NOT NULL,                   -- 이름
    nickname VARCHAR(50) NOT NULL UNIQUE,        -- 닉네임 (중복 불가, 새로 추가)
    sex ENUM('남성', '여성') NOT NULL,           -- 성별
    birthday DATE NOT NULL,                      -- 생일
    phoneNum VARCHAR(15) NOT NULL,               -- 전화번호
    email VARCHAR(100),                          -- 이메일
    postNum VARCHAR(10) NOT NULL,                -- 우편번호
    address VARCHAR(100) NOT NULL,               -- 기본주소
    detailAddress VARCHAR(100) NOT NULL,         -- 상세주소
    profileImg LONGBLOB                          -- 프로필 이미지 (선택사항)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;