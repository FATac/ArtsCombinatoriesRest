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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import org.apache.log4j.Logger;
import org.apache.sanselan.Sanselan;
import org.fundaciotapies.ac.Cfg;
import org.fundaciotapies.ac.model.Request;
import org.fundaciotapies.ac.model.bo.ResourceStatistics;
import org.fundaciotapies.ac.model.bo.Right;
import org.fundaciotapies.ac.model.support.DataMapping;
import org.fundaciotapies.ac.model.support.Template;
import org.fundaciotapies.ac.model.support.TemplateSection;

import com.google.gson.Gson;
import com.sun.media.jai.codec.SeekableStream;

public class ViewGenerator {
	private static Logger log = Logger.getLogger(ViewGenerator.class);
	
	private Template getObjectTemplate(String id) throws Exception {
		
		FileReader fr = null;
		try {
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
				log.warn("Trying to obtain template from no-template object class " + className + "(id " + id + "). Should be in " + Cfg.CONFIGURATIONS_PATH + "mapping/");
				return null;
			}
			
			fr = new FileReader(f);
			return new Gson().fromJson(fr, Template.class);
		} catch (Exception e) {
			throw e;
		} finally {
			if (fr!=null) {
				try { fr.close(); } catch (Exception e) { /* ignorar */ }
			}
		}
	}
	
	private Integer getProfileFromMediaId(String mediaId) {
		int idx1 = mediaId.indexOf("___");
		int idx2 = mediaId.indexOf(".");
		if (idx1<idx2 && idx1>-1 && idx2>-1) {
			String p = mediaId.substring(idx1+3, idx2);
			try {
				return Integer.parseInt(p);
			} catch (NumberFormatException e) {
				return null;
			}
		} else {
			return null;
		}
	}
	
	public void getObjectSectionView(TemplateSection section, String id, String lang, boolean hideMedia) {
		Request req = new Request();
		req.setCurrentLanguage(lang);
		
		for (DataMapping dm : section.getData()) {
			String type = dm.getType();
			if (dm.getPath()==null) continue;
			if ("media".equals(type) && hideMedia) {
				dm.setPath(null);
				String[] value = { Cfg.MEDIA_URL + "forbidden.jpg" , "image" };
				dm.setValue(Arrays.asList(value));
				continue;
			}
			
			if ("linkedObjects".equals(type)) {
				for (String path : dm.getPath()) {
					if (dm.getValue()==null) dm.setValue(new ArrayList<String>());
					dm.getValue().addAll(Arrays.asList(req.resolveModelPath(path, id, true, false, false, true)));
				}
			} else if ("search".equals(type)) {
				String val = (dm.getValue()!=null?dm.getValue().get(0):""); 
				if (dm.getPath()!=null) {
					dm.setValue(new ArrayList<String>());
					for (String path : dm.getPath()) {
						List<String> res = Arrays.asList(req.resolveModelPath(path, id, false, false, false, true));
						for (String r : res) dm.getValue().add(val + r);
					}
				}
			} else if ("counter".equals(type)) { 
				for (String path : dm.getPath()) {
					if (dm.getValue()==null) dm.setValue(new ArrayList<String>());
					dm.getValue().addAll(Arrays.asList(req.resolveModelPath(path, id, false, false, false, false)));
				}
				Map<String, Integer> counter = new HashMap<String, Integer>();
				for (String v : dm.getValue()) {
					Integer c = counter.get(v);
					if (c==null) counter.put(v, 1);
					else counter.put(v, c+1);
				}
				dm.setValue(new ArrayList<String>());
				for (Map.Entry<String, Integer> elem : counter.entrySet()) {
					dm.getValue().add(elem.getKey());
					dm.getValue().add(elem.getValue()+"");
				}
			} else {
				for (String path : dm.getPath()) {
					if (dm.getValue()==null) dm.setValue(new ArrayList<String>());
					dm.getValue().addAll(Arrays.asList(req.resolveModelPath(path, id, false, false, false, true)));
				}
				
				if ("media".equals(type)) {
					List<String> newValues = new ArrayList<String>();
					List<String> values = dm.getValue();
					for (String url : values) {
						newValues.add(url);
						int idx = url.lastIndexOf("/");
						if (idx<0) break;
						String mediaId = url.substring(idx+1);
						String format = req.getObjectFileFormat(mediaId);
						String mediaType = "object";
						if ("jpg,png,jpeg,svg,gif".contains(format)) {
							mediaType = "image";
						} else if ("aif,mp3,ogg,wav,oga".contains(format)) {
							mediaType = "audio";
						} else if ("avi,wma,mp4,ogv".contains(format)) {
							mediaType = "video";
						}
						
						Integer profile = getProfileFromMediaId(mediaId);
						if (profile!=null && Cfg.MEDIA_PROFILES_DESCRIPTION.length >= profile) {
							newValues.add(mediaType+","+Cfg.MEDIA_PROFILES_DESCRIPTION[profile-1]);
						} else {
							newValues.add(mediaType);
						}
					}
					dm.setValue(newValues);
				}
			}
			
			dm.setPath(null);
		}
	}
	
	public Template getObjectView(String id, String sectionName, String uid, String lang) {
		Template template = null;
		
		try {
			Right right = new Right();
			right.load(id);
			
			boolean hideMedia = false;
			int userLegalLevel = new Request().getUserLegalLevel(uid);
			if (right.getRightLevel() !=null && right.getRightLevel() > userLegalLevel && !"".equals(uid)) {
				hideMedia = true;
			}
			
			template = getObjectTemplate(id);
			if (template == null) return null;
			
			template.setClassName(new Request().getObjectClass(id));
			
			for (TemplateSection section : template.getSections()) {
				if (sectionName!=null && !"".equals(sectionName) && !sectionName.contains(section.getName())) {
					section.setData(null);
					continue;
				}
				getObjectSectionView(section, id, lang, hideMedia);
			}
			
			if (sectionName==null || "".equals(sectionName) || sectionName.contains("body")) ResourceStatistics.visit(id);
		} catch (IllegalArgumentException e) {
			log.warn("Object not found " + id);
		} catch (Throwable t) {
			log.error("Error ", t);
		}
		
		return template;
	}
	
	private boolean downloadImage(String path, String tmp) throws Exception {
		boolean downloaded = false;
		
		URL url = new URL(path);
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setRequestProperty("Content-Type", "image/jpg");
		conn.setDoInput(true);
	    conn.setRequestMethod("GET");
	    
	    OutputStream os = new FileOutputStream(Cfg.MEDIA_PATH+"tmp/tmp"+tmp+".jpg");
	    
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
	
	private void resizeImage(BufferedImage img0, BufferedImage img1, BufferedImage img2, BufferedImage img3, String id, boolean toGray) throws Exception {
		if (img0==null) return;
		if (toGray) img0 = toGray(img0);
		
		Color lightGray = new Color(224, 224, 224);
		
		int w = Cfg.THUMBNAIL_WIDTH;
		int h = Cfg.THUMBNAIL_HEIGHT;
		
		BufferedImage resizedImage = new BufferedImage(Cfg.THUMBNAIL_WIDTH, Cfg.THUMBNAIL_HEIGHT, BufferedImage.TYPE_INT_BGR);
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
		
		BufferedImage cutImage = new BufferedImage(scale>1?Math.round(w*scale):w, scale>1?Math.round(h*scale):h, BufferedImage.TYPE_INT_BGR);
		Graphics2D gCut = cutImage.createGraphics();
		gCut.setColor(lightGray);
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
			gCut.setColor(lightGray);
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
			gCut.setColor(lightGray);
			gCut.fillRect(0, 0, cutImage.getWidth(), cutImage.getHeight());
			gCut.drawImage(img2, cutImage.getWidth()/2 - img2.getWidth()/2, cutImage.getHeight()/2 - img2.getHeight()/2, null);
			gResult.drawImage(cutImage, 0, h+Math.round(margin), w, h, null);
			cutImage.flush();
			
			widthScale = img3.getWidth() / w;
			heightScale = img3.getHeight() / h;
			scale = widthScale>heightScale?heightScale:widthScale;
			
			cutImage = new BufferedImage(scale>1?Math.round(w*scale):w, scale>1?Math.round(h*scale):h, img3.getType());
			gCut = cutImage.createGraphics();
			gCut.setColor(lightGray);
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

	public File getClassThumbnail(String className, String style) {
		return getClassThumbnail(className, false, style);
	}
	
	public File getClassThumbnail(String className, Boolean resize, String style) {
		String sStyle = "";
		if (style != null) sStyle = "." + style;
		
		try {
			if (className.indexOf(':')<0) className = Cfg.ONTOLOGY_NAMESPACES[1]+":"+className;
			
			File f = new File(Cfg.MEDIA_PATH + "thumbnails/classes/" + className + sStyle + (resize?"_resized":"") + ".jpg");
			
			if (!f.exists()) {
				List<String> superClasses = new Request().listSuperClasses(className);
				for (String superClassName : superClasses) {
					f = new File(Cfg.MEDIA_PATH + "thumbnails/classes/"+superClassName + sStyle + (resize?"_resized":"") + ".jpg");
					if (f.exists()) break;
				}
			}
			
			if (!f.exists()) {
				if (resize) {
					File tmp = getClassThumbnail(className, style);
					BufferedImage img = ImageIO.read(tmp);
					resizeImage(img, null, null, null, "classes/" + className + sStyle + "_resized", true);
					f = new File(Cfg.MEDIA_PATH + "thumbnails/classes/" + className + sStyle + "_resized.jpg");
				} else {
					f = new File(Cfg.MEDIA_PATH + "thumbnails/classes/default"+sStyle+".jpg");
					if (!f.exists()) f = new File(Cfg.MEDIA_PATH + "thumbnails/classes/default.jpg");
				}
			}
			
			return f;
		} catch (Throwable e) {
			log.error("Error ", e);
		}
		
		return null;
	}
	
	private BufferedImage toGray(BufferedImage img) {
        Color col;
        for (int x = 0; x < img.getWidth(); x++) { //width
            for (int y = 0; y < img.getHeight(); y++) { //height

                int RGBA = img.getRGB(x, y); //gets RGBA data for the specific pixel

                col = new Color(RGBA, true); //get the color data of the specific pixel
                int r = (col.getRed() + 187)/2;
                int g = (col.getGreen() + 187)/2;
                int b = (col.getBlue() + 187)/2;
                
                col = new Color(r,g,b);
                img.setRGB(x, y, col.getRGB()); //set the pixel to the altered colors
            }
        }
        return img;
    }
	
	private BufferedImage negative(BufferedImage img) {
        Color col;
        for (int x = 0; x < img.getWidth(); x++) { //width
            for (int y = 0; y < img.getHeight(); y++) { //height

                int RGBA = img.getRGB(x, y); //gets RGBA data for the specific pixel

                col = new Color(RGBA, true); //get the color data of the specific pixel

                col = new Color(Math.abs(col.getRed() - 255),
                        Math.abs(col.getGreen() - 255), Math.abs(col.getBlue() - 255)); //Swaps values
                //i.e. 255, 255, 255 (white)
                //becomes 0, 0, 0 (black)
                
                img.setRGB(x, y, col.getRGB()); //set the pixel to the altered colors
            }
        }
        return img;
    }
	
	private synchronized BufferedImage loadImage(InputStream in, int module) throws Exception {
		if (module==0) {
			try {
				return Sanselan.getBufferedImage(in);
			} catch (Throwable e1) {
				return null;
			}
		} else if (module==1) {
			try {
				BufferedImage bi = ImageIO.read(in);
				if (bi==null) throw new NullPointerException();
				return bi; 
			} catch (Throwable e2) {
				return null;
			}
		} else if (module==2) {
			try {
				RenderedOp img = JAI.create("stream", SeekableStream.wrapInputStream(in, true));
				return negative(img.getAsBufferedImage());
			} catch (Throwable e3) {
				return null;
			}		
		} else if (module==3) {
			try {
				RenderedOp img = JAI.create("stream", SeekableStream.wrapInputStream(in, true));
				return img.getAsBufferedImage();
			} catch (Throwable e3) {
				return null;
			}
		} else return null;
	}
	
	public synchronized InputStream getObjectThumbnail(String id, String uid, Boolean firstCall) {

		Request req = new Request();
		try {
			Right right = new Right();
			right.load(id);
			
			File f = null;
			
			int userLegalLevel = new Request().getUserLegalLevel(uid);
			if (right.getRightLevel() !=null && right.getRightLevel() > userLegalLevel && !"".equals(uid)) {
				f = new File(Cfg.MEDIA_PATH + "forbidden_thumbnail.jpg");
			} else {
				f = new File(Cfg.MEDIA_PATH + "thumbnails/" + id + ".jpg");
			}
			
			List<String> medias = new ArrayList<String>();
			List<String> subobjects = new ArrayList<String>();
			
			if (!f.exists() && !Cfg.objectClassThumbnail.contains(id)) {
				Template template = getObjectTemplate(id);
				if (template != null) {
					boolean hasThumbnailSection = false;
					for (TemplateSection section : template.getSections()) {
						if ("thumbnail".equals(section.getName())) {
							hasThumbnailSection = true;
							break;
						}
					}
					for (TemplateSection section : template.getSections()) {
						if (hasThumbnailSection && !"thumbnail".equals(section.getName())) continue;
						for (DataMapping d : section.getData()) {
							if (d.getPath()==null) continue;
							for (String path : d.getPath()) {
								if (d.getType().equals("media")) {
									medias.addAll(Arrays.asList(req.resolveModelPath(path, id, false, true, false, true)));
								} else if (d.getType().equals("objects")) {
									subobjects.addAll(Arrays.asList(req.resolveModelPath(path, id, false, true, false, true)));
								}
							}
						}
					}
					
					int count = 0;
					List<BufferedImage> il = new ArrayList<BufferedImage>();
					
					if (medias.size()>0) {
						for (String m : medias) {
							long rand = Math.round(Math.random()*10000);
							if (m.endsWith(".ogv")) m += ".jpg";
							if (downloadImage(m, rand+"")) {
								File df = new File(Cfg.MEDIA_PATH+"tmp/tmp"+rand+".jpg");
								
								if (df.exists()) {
									BufferedImage in = null;
									int mod=0;
									while(in==null && mod<3) in = loadImage(new FileInputStream(df), mod++);
									if (in!=null) {
										il.add(in);
										count++;
									}
									df.delete();
								}
							}
							
							if (count>=4) break;
						}
					}
					
					if (count<4) {
						if (subobjects.size()>0) {
							for (String o : subobjects) {
								InputStream in = getObjectThumbnail(o, "", false);
								if (in!=null) {
									il.add(loadImage(in, 1));
									count++;
								}
								
								if (count>=4) break;
							}
						}
					}
					
					resizeImage(il.size()>0?il.get(0):null, il.size()>1?il.get(1):null, il.size()>2?il.get(2):null, il.size()>3?il.get(3):null, id, false);
					f = new File(Cfg.MEDIA_PATH + "thumbnails/" + id + ".jpg");
				}
			}

			if (!f.exists() && firstCall) {
				String className = new Request().getObjectClass(id);
				f = getClassThumbnail(className, firstCall, null);
				Cfg.objectClassThumbnail.add(id);
			}
			
			if (f.exists())	return new FileInputStream(f);
		} catch (Throwable e) {
			log.warn("Error " + e, e);
		}
		
		return null;
	}

}
