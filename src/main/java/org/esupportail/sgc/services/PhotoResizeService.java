package org.esupportail.sgc.services;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

import javax.imageio.ImageIO;

import org.esupportail.sgc.domain.PhotoFile;
import org.springframework.stereotype.Service;

@Service
public class PhotoResizeService {

	public byte[] resizePhoto(PhotoFile photoFile, int width, int height) throws IOException, SQLException {
		InputStream photoStream = new ByteArrayInputStream(photoFile.getBigFile().getBinaryFileasBytes());
		BufferedImage photoImg = ImageIO.read(photoStream);
		Image photoResizedImg = photoImg.getScaledInstance(width, height, Image.SCALE_DEFAULT);
		BufferedImage bufferedImage = new BufferedImage(photoResizedImg.getWidth(null), photoResizedImg.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = bufferedImage.createGraphics();
        g2.drawImage(photoResizedImg, null, null);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(bufferedImage, "jpg", baos );
		byte[] vignetteImgBytes = baos.toByteArray();
		return vignetteImgBytes;
	}
	
	
}
