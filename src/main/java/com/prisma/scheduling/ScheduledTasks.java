package com.prisma.scheduling;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.prisma.service.ProcessFileService;

@Component
public class ScheduledTasks {

	private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

	@Value("${app.inputFile}")
	private String inputFile;
	@Value("${app.outputFile}")
	private String outputFile;
	@Value("${app.temporalStoralFile}")
	private String temporalStoralFile;
	@Value("${app.localDateTime}")
	private String localDateTimeFormat;

	private ProcessFileService processfileservice;

	@Autowired
	public void setProcessfileservice(ProcessFileService processfileservice) {
		this.processfileservice = processfileservice;
	}

	@Scheduled(fixedRateString = "${intervalTime}")
	public void consumeFiles() {
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(localDateTimeFormat);
		String formatDateTime = now.format(formatter);
		log.info("The time is now {}", formatDateTime);
//		log.info("inputFile {}",inputFile);
//		log.info("outputFile {}", outputFile);
//		log.info("temporalStoralFile {}", temporalStoralFile);

		Consumer<Path> consumer = a -> processfileservice.processPdfFiles(a, formatDateTime, outputFile,
				temporalStoralFile);

		try (Stream<Path> pathsFiles = Files.walk(Paths.get(inputFile))) {
			pathsFiles.filter(Files::isRegularFile).filter(p -> p.getFileName().toString().endsWith(".pdf"))
					.forEach(consumer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}