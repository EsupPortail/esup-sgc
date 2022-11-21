package org.esupportail.sgc.tools;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class EsupSgcBmpAsBase64Util {

	static Logger log = LoggerFactory.getLogger(EsupSgcBmpAsBase64Util.class);

    public static String getBmpCard(Long cardId, String type) {
        StopWatch stopWatch = new PrettyStopWatch();
        stopWatch.start("getBmpCard for card " + cardId + " / " + type);
        String bmpCard = "";
        try {
            String tmpdir = Files.createTempDirectory(cardId.toString() + "-" + type).toFile().getAbsolutePath();
            String command = "cd " + tmpdir + " " +
                    "&& wget -4 'http://localhost:8080/wsrest/view/" + cardId + "/card-b64.html?type=" + type + "' -O card-b64.html " +
                    "&& google-chrome --headless --disable-gpu --print-to-pdf=card.pdf card-b64.html " +
                    "&& convert -resize 1016x648 -gravity center -extent 1016x648 -density 600 card.pdf card.bmp";
            log.info("Convertion command to get BMP for card {} / {} : {}", cardId, type, command);
            ProcessBuilder processBuilder = new ProcessBuilder();
            if (SystemUtils.IS_OS_WINDOWS) {
                processBuilder.command("cmd", "/C", command);
            } else {
                processBuilder.command("bash", "-c", command);
            }
            Process process = processBuilder.start();
            int exitVal = process.waitFor();
            if (exitVal == 0) {
                log.info("Convert success");
                File bmpFile = new File(tmpdir + "/card.bmp");
                byte[] encodedBmp = Base64.encodeBase64(FileUtils.readFileToByteArray(bmpFile));
                bmpCard = new String(encodedBmp, StandardCharsets.US_ASCII);
                stopWatch.stop();
                log.info(stopWatch.prettyPrint());
            } else {
                StringWriter errorWriter = new StringWriter();
                String encoding = StandardCharsets.UTF_8.name();
                IOUtils.copy(process.getErrorStream(), errorWriter, encoding);
                String error = errorWriter.toString();
                log.error("cmd {} failed : {}", error);
                process.getErrorStream();
                process.destroy();
                throw new RuntimeException(error);
            }
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException("cmd launch error : check installation", e);
        }
        return bmpCard;
    }

}
