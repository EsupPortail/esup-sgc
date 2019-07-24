package org.esupportail.sgc.tools;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageUtils {
	
	private static final Logger log = LoggerFactory.getLogger(ImageUtils.class);
	
	private static final int IMG_WIDTH = 300;
	
	private static final int IMG_HEIGHT = 376;
	
	private static final String CONTENT_TYPE = "image/jpeg";
	

	public static String getContentType() {
		return CONTENT_TYPE;
	}

	public static byte[] resizeImage(byte[] bytes) {
		try {
			BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(bytes));
			int type = originalImage.getType() == 0 ? BufferedImage.TYPE_3BYTE_BGR : originalImage.getType();
			if(BufferedImage.TYPE_4BYTE_ABGR == type) {
				log.trace("BufferedImage.TYPE_4BYTE_ABGR can't be handled on openjdk - we use  BufferedImage.TYPE_3BYTE_BGR instead");
				type = BufferedImage.TYPE_3BYTE_BGR;
			}
			BufferedImage resizedImage = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, type);
			Graphics2D g = resizedImage.createGraphics();
	        g.setColor(Color.WHITE);
	        g.fillRect(0, 0, IMG_WIDTH, IMG_HEIGHT);
			g.drawImage(originalImage, 0, 0, IMG_WIDTH, IMG_HEIGHT, null);
			g.dispose();
			g.setComposite(AlphaComposite.Src);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(resizedImage, "jpeg", baos );
			baos.flush();
			byte[] imageInByte = baos.toByteArray();
			baos.close();
			return imageInByte;
		} catch(IOException e) {
			log.error("IOException resizing image", e);
			throw new RuntimeException(e);
		}
		
	}

}
