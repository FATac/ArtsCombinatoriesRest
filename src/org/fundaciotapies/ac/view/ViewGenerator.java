package org.fundaciotapies.ac.view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.fundaciotapies.ac.Cfg;
import org.fundaciotapies.ac.model.Request;
import org.fundaciotapies.ac.model.bo.ResourceStatistics;
import org.fundaciotapies.ac.model.bo.Right;
import org.fundaciotapies.ac.model.support.DataMapping;
import org.fundaciotapies.ac.model.support.Template;
import org.fundaciotapies.ac.model.support.TemplateSection;

import com.google.gson.Gson;

public class ViewGenerator {
	private static Logger log = Logger.getLogger(ViewGenerator.class);
	
	private Template getObjectTemplate(String id) throws Exception {
		Request req = new Request();
		String className = req.getObjectClass(id);
		File f = new File(Cfg.CONFIGURATIONS_PATH+"mapping/"+className+".json");
		
		if (!f.exists()) {
			List<String> superClasses = req.listSuperClasses(className);
			for (String superClassName : superClasses) {
				f = new File(Cfg.CONFIGURATIONS_PATH+"mapping/"+superClassName+".json");
				if (f.exists()) break;
			}
		}
		
		if (!f.exists()) { 
			log.warn("Trying to obtain template from no-template object class " + className + "(id " + id + ")");
			return null;
		}
		
		return new Gson().fromJson(new FileReader(f), Template.class);
	}
	
	public void getObjectSectionView(TemplateSection section, String id, String lang) {
		Request req = new Request();
		req.setCurrentLanguage(lang);
		
		for (DataMapping dm : section.getData()) {
			String type = dm.getType();
			
			if ("linkedObjects".equals(type)) {
				for (String path : dm.getPath()) {
					if (dm.getValue()==null) dm.setValue(new ArrayList<String>());
					dm.getValue().addAll(Arrays.asList(req.resolveModelPath(path, id, true, false, false)));
				}
			} else if ("search".equals(type)) {
				String val = (dm.getValue()!=null?dm.getValue().get(0):""); 
				if (dm.getPath()!=null) {
					dm.setValue(new ArrayList<String>());
					for (String path : dm.getPath()) {
						List<String> res = Arrays.asList(req.resolveModelPath(path, id, false, false, false));
						for (String r : res) dm.getValue().add(val + r);
					}
				}
			} else {
				for (String path : dm.getPath()) {
					if (dm.getValue()==null) dm.setValue(new ArrayList<String>());
					dm.getValue().addAll(Arrays.asList(req.resolveModelPath(path, id, false, false, false)));
				}
			}
			
			dm.setPath(null);
		}
	}
	
	public Template getObjectView(String id, String uid, String lang) {
		Template template = null;
		
		try {
			Right right = new Right();
			right.load(id);
			
			int userLegalLevel = new Request().getUserLegalLevel(uid);
			if (right.getRightLevel() !=null && right.getRightLevel() > userLegalLevel && !"".equals(uid)) {
				throw new Exception("Access to object denied due to legal restrictions");
			}
			
			template = getObjectTemplate(id);
			if (template == null) return null;
			
			for (TemplateSection section : template.getSections()) getObjectSectionView(section, id, lang);
			
			ResourceStatistics.visit(id);
			
		} catch (Throwable e) {
			log.error("Error ", e);
		}
		
		return template;
	}
	
	private boolean downloadImage(String path) throws Exception {
		boolean downloaded = false;
		
		File f = new File(Cfg.MEDIA_PATH+"tmp.jpg");
		if (f.exists()) f.delete();
		
		URL url = new URL(path);
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setRequestProperty("Content-Type", "image/jpg");
		conn.setDoInput(true);
	    conn.setRequestMethod("GET");
	    
	    OutputStream os = new FileOutputStream(Cfg.MEDIA_PATH+"tmp.jpg");
	    
	    InputStream is = conn.getInputStream();
	    byte[] buffer = new byte[1024];
	    int readLength = is.read(buffer);
	    while(readLength != -1)
	    {
	    	downloaded = true;
	        os.write(buffer,0,readLength);
	        readLength = is.read(buffer);
	    }
	    
	    os.close();
	    
	    return downloaded;
	}
	
	private void resizeImage(BufferedImage img0, BufferedImage img1, BufferedImage img2, BufferedImage img3, String id) throws Exception {
		if (img0==null) return;
		
		int w = Cfg.THUMBNAIL_WIDTH;
		int h = Cfg.THUMBNAIL_HEIGHT;
		
		BufferedImage resizedImage = new BufferedImage(Cfg.THUMBNAIL_WIDTH, Cfg.THUMBNAIL_HEIGHT, img0.getType());
		Graphics2D gResult = resizedImage.createGraphics();
		gResult.setColor(Color.WHITE);
		gResult.fillRect(0, 0, Cfg.THUMBNAIL_WIDTH, Cfg.THUMBNAIL_HEIGHT);
		
		float margin = w*0.04f;
		
		if (img1!=null && img2==null) w = Math.round((w/2) - margin/2);
		if (img2!=null && img3!=null) {
			w = Math.round((w/2) - margin/2);
			h = Math.round((h/2) - margin/2);
		}
		
		float widthScale = img0.getWidth() / w;
		float heightScale = img0.getHeight() / h;
		float scale = widthScale>heightScale?heightScale:widthScale;
		
		BufferedImage cutImage = new BufferedImage(scale>1?Math.round(w*scale):w, scale>1?Math.round(h*scale):h, img0.getType());
		Graphics2D gCut = cutImage.createGraphics();
		gCut.setColor(Color.WHITE);
		gCut.fillRect(0, 0, cutImage.getWidth(), cutImage.getHeight());
		gCut.drawImage(img0, cutImage.getWidth()/2 - img0.getWidth()/2, cutImage.getHeight()/2 - img0.getHeight()/2, null);
		gResult.drawImage(cutImage, 0, 0, w, h, null);
		cutImage.flush();
		
		if (img1!=null) {
			widthScale = img1.getWidth() / w;
			heightScale = img1.getHeight() / h;
			scale = widthScale>heightScale?heightScale:widthScale;
			
			cutImage = new BufferedImage(scale>1?Math.round(w*scale):w, scale>1?Math.round(h*scale):h, img1.getType());
			gCut = cutImage.createGraphics();
			gCut.setColor(Color.WHITE);
			gCut.fillRect(0, 0, cutImage.getWidth(), cutImage.getHeight());
			gCut.drawImage(img1, cutImage.getWidth()/2 - img1.getWidth()/2, cutImage.getHeight()/2 - img1.getHeight()/2, null);
			gResult.drawImage(cutImage, w+Math.round(margin), 0, w, h, null);
			cutImage.flush();
		} 
		
		if (img2!=null && img3!=null) {
			widthScale = img2.getWidth() / w;
			heightScale = img2.getHeight() / h;
			scale = widthScale>heightScale?heightScale:widthScale;
			
			cutImage = new BufferedImage(scale>1?Math.round(w*scale):w, scale>1?Math.round(h*scale):h, img2.getType());
			gCut = cutImage.createGraphics();
			gCut.setColor(Color.WHITE);
			gCut.fillRect(0, 0, cutImage.getWidth(), cutImage.getHeight());
			gCut.drawImage(img2, cutImage.getWidth()/2 - img2.getWidth()/2, cutImage.getHeight()/2 - img2.getHeight()/2, null);
			gResult.drawImage(cutImage, 0, h+Math.round(margin), w, h, null);
			cutImage.flush();
			
			widthScale = img3.getWidth() / w;
			heightScale = img3.getHeight() / h;
			scale = widthScale>heightScale?heightScale:widthScale;
			
			cutImage = new BufferedImage(scale>1?Math.round(w*scale):w, scale>1?Math.round(h*scale):h, img3.getType());
			gCut = cutImage.createGraphics();
			gCut.setColor(Color.WHITE);
			gCut.fillRect(0, 0, cutImage.getWidth(), cutImage.getHeight());
			gCut.drawImage(img3, cutImage.getWidth()/2 - img3.getWidth()/2, cutImage.getHeight()/2 - img3.getHeight()/2, null);
			gResult.drawImage(cutImage, w+Math.round(margin), h+Math.round(margin), w, h, null);
			cutImage.flush();
		}
		
		gResult.dispose();
		gCut.dispose();
		
		File f = new File(Cfg.MEDIA_PATH + "thumbnails/" + id + ".jpg");
		ImageIO.write(resizedImage, "jpg", f);
	}

	
	public File getClassThumbnail(String className) {
		try {
			File f = new File(Cfg.MEDIA_PATH + "thumbnails/classes/" + className + ".jpg");
			
			if (!f.exists()) {
				List<String> superClasses = new Request().listSuperClasses(className);
				for (String superClassName : superClasses) {
					f = new File(Cfg.MEDIA_PATH + "thumbnails/classes/"+superClassName+".jpg");
					if (f.exists()) break;
				}
			}
			
			if (!f.exists()) f = new File(Cfg.MEDIA_PATH + "thumbnails/classes/default.jpg");
			
			return f;
		} catch (Throwable e) {
			log.error("Error ", e);
		}
		
		return null;
	}
	
	public InputStream getObjectThumbnail(String id, String uid) {
		Request req = new Request();
		try {
			Right right = new Right();
			right.load(id);
			
			int userLegalLevel = new Request().getUserLegalLevel(uid);
			if (right.getRightLevel() !=null && right.getRightLevel() > userLegalLevel && !"".equals(uid)) {
				throw new Exception("Access to object denied due to legal restrictions");
			}
			
			File f = new File(Cfg.MEDIA_PATH + "thumbnails/" + id + ".jpg");
			
			List<String> medias = new ArrayList<String>();
			List<String> subobjects = new ArrayList<String>();
			
			if (!f.exists()) {
			
				Template template = getObjectTemplate(id);
				if (template != null) {
					for (TemplateSection section : template.getSections()) {
						for (DataMapping d : section.getData()) {
							for (String path : d.getPath()) {
								if (d.getType().equals("media")) {
									medias.addAll(Arrays.asList(req.resolveModelPath(path, id, false, true, false)));
								} else if (d.getType().equals("objects")) {
									subobjects.addAll(Arrays.asList(req.resolveModelPath(path, id, false, true, false)));
								}
							}
						}
					}
					
					int count = 0;
					List<BufferedImage> il = new ArrayList<BufferedImage>();
					
					if (medias.size()>0) {
						for (String m : medias) {
							if (downloadImage(m)) {
								il.add(ImageIO.read(new File(Cfg.MEDIA_PATH+"tmp.jpg")));
								count++;
							}
							
							if (count>=4) break;
						}
					} else if (subobjects.size()>0) {					
						for (String o : subobjects) {
							InputStream in = getObjectThumbnail(o, "");
							if (in!=null) {
								il.add(ImageIO.read(in));
								count++;
							}
							
							if (count>=4) break;
						}
					}
					
					resizeImage(il.size()>0?il.get(0):null, il.size()>1?il.get(1):null, il.size()>2?il.get(2):null, il.size()>3?il.get(3):null, id);
					f = new File(Cfg.MEDIA_PATH + "thumbnails/" + id + ".jpg");
				}
			}

			if (!f.exists()) {
				String className = new Request().getObjectClass(id);
				f = getClassThumbnail(className);
			}
			
			if (f.exists())	return new FileInputStream(f);
		} catch (Throwable e) {
			log.error("Error ", e);
		}
		
		return null;
	}

}
