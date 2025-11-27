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

--after creating both dbs
INSERT INTO courses
(code, title, credits)
VALUES
('MTH201', 'PnS', 4),
('CSE101', 'IP', 4),
('ECE111', 'DC', 4),
('MTH203', 'M-III', 4);

INSERT INTO sections
(course_id, instructor_id, day_time, room, capacity, semester, year)
VALUES
(10, 7, 'Mon 09:30–11:00 / Wed 09:30–11:00', 'C201', 300, 'Monsoon', 2025),
(11, 2, 'Monday 11:00–12:30 / Wednesday 11:00–12:30', 'C101', 300, 'Monsoon', 2025),
(13, 5, 'Tue 09:30–11:00 / Thu 11:00–12:30', 'C101', 300, 'Monsoon', 2025),
(14, 9, 'Tuesday 16:30–18:00 / Thursday 16:30–18:00', 'C201', 300, 'Monsoon', 2025);


INSERT INTO settings (`key`, `value`)
VALUES
('registration_deadline', '2025-12-31'),
('maintenance_on', 'false');