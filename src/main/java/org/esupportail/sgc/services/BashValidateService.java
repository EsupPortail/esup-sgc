package org.esupportail.sgc.services;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.tools.PrettyStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class BashValidateService extends ValidateService {

	private final Logger log = LoggerFactory.getLogger(getClass());

	String validateBashCmd = null;

	String invalidateBashCmd = null;

	public void setValidateBashCmd(String validateBashCmd) {
		this.validateBashCmd = validateBashCmd;
	}

	public void setInvalidateBashCmd(String invalidateBashCmd) {
		this.invalidateBashCmd = invalidateBashCmd;
	}

	@Override
	public void validateInternal(Card card) {
		if(!StringUtils.isEmpty(validateBashCmd)) {
			callBash(validateBashCmd, card);
		}
	}

	@Override
	public void invalidateInternal(Card card) {
		if(!StringUtils.isEmpty(invalidateBashCmd)) {
			callBash(invalidateBashCmd, card);
		}
	}

	protected void callBash(String bashCmd, Card card) {
		Long cardId = card.getId();
		File tmpdirFile = null;
		try {
			tmpdirFile = Files.createTempDirectory(cardId.toString() + "-bash-cmd-").toFile();
			tmpdirFile.deleteOnExit();
			String tmpdir = tmpdirFile.getAbsolutePath();
			String command =  "cd " + tmpdir + " && " +  String.format(bashCmd, card.getCsn(), card.getEppn());
			log.info("(In)Validation command for card {} / {} for {} : {}", cardId, card.getCsn(), card.getEppn(), command);
			StopWatch stopWatch = new PrettyStopWatch();
			stopWatch.start(String.format("callBash for card %s / %s : %s", cardId, card.getCsn(), command));
			ProcessBuilder processBuilder = new ProcessBuilder();
			if (SystemUtils.IS_OS_WINDOWS) {
				processBuilder.command("cmd", "/C", command);
			} else {
				processBuilder.command("bash", "-c", command);
			}
			Process process = processBuilder.start();
			int exitVal = process.waitFor();
			if (exitVal == 0) {
				log.info("Cmd success");
				stopWatch.stop();
				log.info(stopWatch.prettyPrint());
				// log output of process
				StringWriter writer = new StringWriter();
				String encoding = StandardCharsets.UTF_8.name();
				IOUtils.copy(process.getInputStream(), writer, encoding);
				String output = writer.toString();
				log.info("cmd {} output : {}", command, output);
				// log error of process
				StringWriter errorWriter = new StringWriter();
				IOUtils.copy(process.getErrorStream(), errorWriter, encoding);
				String error = errorWriter.toString();
				if(!error.isEmpty()) {
					log.error("cmd {} error : {}", command, error);
				}
				process.destroy();
			} else {
				StringWriter errorWriter = new StringWriter();
				String encoding = StandardCharsets.UTF_8.name();
				IOUtils.copy(process.getErrorStream(), errorWriter, encoding);
				String error = errorWriter.toString();
				log.error("cmd {} failed : {}", command, error);
				process.getErrorStream();
				process.destroy();
				throw new RuntimeException(error);
			}
		} catch (InterruptedException | IOException e) {
			throw new RuntimeException("cmd launch error : check installation", e);
		} finally {
			if(tmpdirFile!=null) {
				try {
					FileUtils.deleteDirectory(tmpdirFile);
				} catch (IOException e) {
					log.warn("Can't delete " + tmpdirFile.getPath(), e);
				}
			}
		}
	}
}

