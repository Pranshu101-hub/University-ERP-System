CREATE DATABASE IF NOT EXISTS erp_db;
USE erp_db;

CREATE TABLE IF NOT EXISTS students (
  user_id INT PRIMARY KEY,
  roll_no VARCHAR(50) UNIQUE,
  program VARCHAR(100),
  year INT
);

CREATE TABLE IF NOT EXISTS instructors (
  user_id INT PRIMARY KEY,
  department VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS courses (
  course_id INT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(20) UNIQUE NOT NULL,
  title VARCHAR(255) NOT NULL,
  credits INT NOT NULL
);

CREATE TABLE IF NOT EXISTS sections (
  section_id INT AUTO_INCREMENT PRIMARY KEY,
  course_id INT NOT NULL,
  instructor_id INT,
  day_time VARCHAR(100),
  room VARCHAR(50),
  capacity INT NOT NULL,
  semester VARCHAR(10),
  year INT,
  FOREIGN KEY (course_id) REFERENCES courses(course_id)
);

CREATE TABLE IF NOT EXISTS enrollments (
  enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
  student_id INT NOT NULL,
  section_id INT NOT NULL,
  status ENUM('ENROLLED','DROPPED','COMPLETED') DEFAULT 'ENROLLED',
  UNIQUE (student_id, section_id)
);

CREATE TABLE IF NOT EXISTS grades (
  grade_id INT AUTO_INCREMENT PRIMARY KEY,
  enrollment_id INT NOT NULL,
  component VARCHAR(50),
  score DOUBLE,
  FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id)
);

CREATE TABLE IF NOT EXISTS settings (
  setting_key VARCHAR(100) PRIMARY KEY,
  setting_value VARCHAR(255)
);