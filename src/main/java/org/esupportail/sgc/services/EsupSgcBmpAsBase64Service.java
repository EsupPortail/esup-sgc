package org.esupportail.sgc.services;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.esupportail.sgc.tools.PrettyStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Service
public class EsupSgcBmpAsBase64Service {

	static Logger log = LoggerFactory.getLogger(EsupSgcBmpAsBase64Service.class);

    @Resource
    AppliConfigService appliConfigService;

    public String getBmpCard(Long cardId, String type) {
        StopWatch stopWatch = new PrettyStopWatch();
        stopWatch.start("getBmpCard for card " + cardId + " / " + type);
        String bmpCard = "";
        File tmpdirFile = null;
        try {
            String bmpCardCommand = appliConfigService.getBmpCardCommandColor4printer();
            if("black".equals(type)) {
                bmpCardCommand = appliConfigService.getBmpCardCommandBlack4printer();
            }
            tmpdirFile = Files.createTempDirectory(cardId.toString() + "-" + type).toFile();
            tmpdirFile.deleteOnExit();
            String tmpdir = tmpdirFile.getAbsolutePath();
            String command =  "cd " + tmpdir + " && " +  String.format(bmpCardCommand, cardId);
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
        } finally {
            if(tmpdirFile!=null) {
                tmpdirFile.delete();
            }
        }
        return bmpCard;
    }

}
