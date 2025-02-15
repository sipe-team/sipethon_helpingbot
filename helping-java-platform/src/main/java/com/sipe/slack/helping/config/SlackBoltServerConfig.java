package com.sipe.slack.helping.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;

import com.sipe.slack.helping.HangOut;
import com.sipe.slack.helping.attendance.FindMeAttendanceCommand;
import com.sipe.slack.helping.Attendance;
import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import com.slack.api.bolt.socket_mode.SocketModeApp;
import com.slack.api.socket_mode.SocketModeClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SlackBoltServerConfig {

	private final FindMeAttendanceCommand findAttendanceCommand;
	private final Attendance attendance;
	private final HangOut hangOut;

	@Bean
	@EventListener(ContextStartedEvent.class)
	public SocketModeApp startSocketModeApp() throws Exception {
		log.info("Start socket mode slack bolt app server.");

		String botToken = System.getenv("SLACK_BOT_TOKEN");
		String appToken = System.getenv("SLACK_APP_TOKEN");
		String signingSecret = System.getenv("SLACK_SIGNING_SECRET");

		App app = new App(AppConfig.builder()
			.singleTeamBotToken(botToken)
			.signingSecret(signingSecret)
			.build());

		// 본인 출석여부 확인 커맨드
		app.command("/출석여부", findAttendanceCommand.findMeAttendanceCommand(botToken));
		app.command("/출석", attendance.attendance());
		app.viewSubmission("attendance", attendance.handleSubmission());
		app.command("/뒷풀이", hangOut.HangoutHandler());
		app.viewSubmission("hangout_view", hangOut.handleHangoutSubmission());
		SocketModeApp socketModeApp = new SocketModeApp(appToken, SocketModeClient.Backend.JavaWebSocket, app);
		socketModeApp.startAsync();
		return socketModeApp;
	}

}