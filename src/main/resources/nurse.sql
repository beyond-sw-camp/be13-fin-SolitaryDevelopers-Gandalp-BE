-- TABLE [NURSE]
-- [중앙대학교광명병원 가정의학과] 간호사 20명 + HEAD_NURSE 1명
INSERT INTO nurse (
    `no`, `email`, `name`, `password`, `department-id`, `type`, `working_status`,
    `created_at`, `updated_at`, `created_by`, `updated_by`
) VALUES
      ('aa7429c2-f332-449a-8277-b28e7567bad4', 'kimminjun259@gmail.com', '김민준', '$2a$10$nJhgYCVWYNMS2mtABhnt6OwKTFZAAxhIDmR0Zeu55TETWDGogdGm2', 1, 'NURSE', 'OFF', NOW(), NOW(), 'headnurse', 'headnurse'),
      ('76b47812-90bb-43dc-9537-38c82dad4fe0', 'leeseoyeon696@naver.com', '이서연', '$2a$10$nJhgYCVWYNMS2mtABhnt6OwKTFZAAxhIDmR0Zeu55TETWDGogdGm2', 1, 'NURSE', 'OFF', NOW(), NOW(), 'headnurse', 'headnurse'),
      ('23122356-d6ee-4b0d-bcbc-8349f93e220c', 'ohjimin553@daum.net', '오지민', '$2a$10$nJhgYCVWYNMS2mtABhnt6OwKTFZAAxhIDmR0Zeu55TETWDGogdGm2', 1, 'NURSE', 'ON', NOW(), NOW(), 'headnurse', 'headnurse'),
      ('2acffd4e-f974-4298-8808-f4e724deacb4', 'kanghayul61@daum.net', '강하율', '$2a$10$nJhgYCVWYNMS2mtABhnt6OwKTFZAAxhIDmR0Zeu55TETWDGogdGm2', 1, 'NURSE', 'IN_SURGERY', NOW(), NOW(), 'headnurse', 'headnurse'),
      ('7b4e6df5-662f-4f46-88b5-fbeb0c037724', 'baesua82@daum.net', '배수아', '$2a$10$nJhgYCVWYNMS2mtABhnt6OwKTFZAAxhIDmR0Zeu55TETWDGogdGm2', 1, 'NURSE', 'IN_SURGERY', NOW(), NOW(), 'headnurse', 'headnurse'),
      ('56d33d32-8ec5-40bb-94b6-dd01f5e37591', 'notaehyun191@gmail.com', '노태현', '$2a$10$nJhgYCVWYNMS2mtABhnt6OwKTFZAAxhIDmR0Zeu55TETWDGogdGm2', 1, 'NURSE', 'ON', NOW(), NOW(), 'headnurse', 'headnurse'),
      ('52e4d9bc-625e-41ae-ab92-86b5b5909ab4', 'moondain92@gmail.com', '문다인', '$2a$10$nJhgYCVWYNMS2mtABhnt6OwKTFZAAxhIDmR0Zeu55TETWDGogdGm2', 1, 'NURSE', 'IN_SURGERY', NOW(), NOW(), 'headnurse', 'headnurse'),
      ('50cbe569-11ce-4d40-bcbf-ad16614e3873', 'imyerin106@daum.net', '임예린', '$2a$10$nJhgYCVWYNMS2mtABhnt6OwKTFZAAxhIDmR0Zeu55TETWDGogdGm2', 1, 'NURSE', 'IN_SURGERY', NOW(), NOW(), 'headnurse', 'headnurse'),
      ('a6a6069b-0b1e-4125-a1f9-bf6afde5c0e2', 'baekseunghyun706@naver.com', '백승현', '$2a$10$nJhgYCVWYNMS2mtABhnt6OwKTFZAAxhIDmR0Zeu55TETWDGogdGm2', 1, 'NIGHT_NURSE', 'OFF', NOW(), NOW(), 'headnurse', 'headnurse'),
      ('9f561567-4e11-456e-9a47-293f26f8dfe6', 'hongseojin781@daum.net', '홍서진', '$2a$10$nJhgYCVWYNMS2mtABhnt6OwKTFZAAxhIDmR0Zeu55TETWDGogdGm2', 1, 'NIGHT_NURSE', 'OFF', NOW(), NOW(), 'headnurse', 'headnurse'),
      ('104b31de-99f0-4246-900d-1aa7cf1b4727', 'shinjaeyoon841@gmail.com', '신재윤', '$2a$10$nJhgYCVWYNMS2mtABhnt6OwKTFZAAxhIDmR0Zeu55TETWDGogdGm2', 1, 'NIGHT_NURSE', 'ON', NOW(), NOW(), 'headnurse', 'headnurse'),
      ('00000000-0000-0000-0000-000000000001', 'headnurse@hospital.com', '최간호', '$2a$10$nJhgYCVWYNMS2mtABhnt6OwKTFZAAxhIDmR0Zeu55TETWDGogdGm2', 1, 'HEAD_NURSE', 'ON', NOW(), NOW(), 'system', 'system');
