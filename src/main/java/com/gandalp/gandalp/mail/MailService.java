package com.gandalp.gandalp.mail;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import com.gandalp.gandalp.member.domain.entity.Nurse;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MailService {

	// 보내는 사람
	@Value("${spring.mail.username}")
	String sender;

	@Autowired
	private JavaMailSender javaMailSender;

	public void sendCommentNotificationMail(Nurse boardNurse, Nurse commentNurse) {
		SimpleMailMessage simpleMailMessage = new SimpleMailMessage();

		try {
			// 수신자: 게시글 작성자
			simpleMailMessage.setTo(boardNurse.getEmail());

			// 메일 제목
			simpleMailMessage.setSubject(String.format("[교대 알림] %s님, 교대 요청에 댓글이 등록되었습니다.", boardNurse.getName()));

			// 송신자
			String from = String.format("%s <" + sender + ">", boardNurse.getDepartment().getHospital().getName());
			simpleMailMessage.setFrom(from);

			// 날짜 포맷
			String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"));

			// 메일 본문
			String message = String.format("""
				안녕하세요.
		
				%s 기준으로 %s님이 등록하신 교대 요청 게시글에
				%s님께서 댓글을 남기셨습니다.
		
				자세한 내용은 시스템에서 확인하실 수 있습니다.
		
				감사합니다.
				""", date, boardNurse.getName(), commentNurse.getName());

			simpleMailMessage.setText(message);

			// 메일 전송
			javaMailSender.send(simpleMailMessage);

			log.info("📌 댓글 알림 메일 발송 성공");

		} catch (Exception e) {
			log.error("메일 발송 실패", e);
			throw new RuntimeException(e);
		}
	}


	public void sendSimpleMailMessage(Nurse boardNurse, Nurse commentNurse) {
		SimpleMailMessage simpleMailMessage = new SimpleMailMessage();

		try {

			// 수신자 설정
			simpleMailMessage.setTo(boardNurse.getEmail(),commentNurse.getEmail());

			// 메일 제목 설정
			simpleMailMessage.setSubject(String.format("[근무 알림] %s님과 %s님의 근무시간이 변경되었습니다.",
				boardNurse.getName(), commentNurse.getName()));

			// 송신자 메일 주소
			String from = String.format("%s <" + sender + ">", boardNurse.getDepartment().getHospital().getName());
			simpleMailMessage.setFrom(from);

			// 변경 날짜
			String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"));


			// 메일 내용 설정
			String message = String.format("""
            안녕하세요.

            %s 기준으로, %s님과 %s님의 근무 시간이 변경되어 안내드립니다.

            변경된 근무 내용은 시스템에서 확인하실 수 있으며,
            업무에 차질 없도록 확인 부탁드립니다.

            감사합니다.
            """,date, boardNurse.getName(), commentNurse.getName());

			simpleMailMessage.setText(message);


			// 발송
			javaMailSender.send(simpleMailMessage);


			log.info("📌 메일 발송 성공");

		}catch(Exception e){
			log.info("메일 발송 실패");
			throw new RuntimeException(e);
		}
	}
}
