
-- TABLE [MEMBER] : 비번은 일단 다 1234
-- ADMIN 계정 1개
INSERT INTO member (
    `hospital-id`, `department-id`, `account_id`, `password`, `type`, `created_at`, `updated_at`, `created_by`, `updated_by`
) VALUES (
             1, null, 'admin', '$2y$04$uVaWlH5kKz9Tygw0VHrkNeHpimT90ESp8jnIjWnRSYK/Zx4kbVFTG', 'ADMIN',
             NOW(), NOW(), 'system', 'system');


-- [강릉아산병원 응급의학과] - 수간호사 계정 1개, 일반 간호사 계정 1개
INSERT INTO member (
    `hospital-id`, `department-id`, `account_id`, `password`, `type`,
    `created_at`, `updated_at`, `created_by`, `updated_by`
) VALUES (
             1, 1, 'headnurse', '$2y$04$uVaWlH5kKz9Tygw0VHrkNeHpimT90ESp8jnIjWnRSYK/Zx4kbVFTG', 'HEAD_NURSE',
             NOW(), NOW(), 'admin', 'admin');

INSERT INTO member (
    `hospital-id`, `department-id`, `account_id`, `password`, `type`,
    `created_at`, `updated_at`, `created_by`, `updated_by`
) VALUES (
             1, 1, 'nurse', '$2y$04$uVaWlH5kKz9Tygw0VHrkNeHpimT90ESp8jnIjWnRSYK/Zx4kbVFTG', 'NURSE',
             NOW(), NOW(), 'admin', 'admin');

INSERT INTO member (
    `hospital-id`, `department-id`, `account_id`, `password`, `type`,
    `created_at`, `updated_at`, `created_by`, `updated_by`
) VALUES (
             null, null, 'paramedic', '$2y$04$uVaWlH5kKz9Tygw0VHrkNeHpimT90ESp8jnIjWnRSYK/Zx4kbVFTG', 'PARAMEDIC',
             NOW(), NOW(), 'admin', 'admin');
