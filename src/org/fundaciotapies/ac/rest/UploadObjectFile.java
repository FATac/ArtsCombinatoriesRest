package org.fundaciotapies.ac.rest;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;
import org.fundaciotapies.ac.model.Upload;

/**
 * Call: http://{host:port}/{class}/{id}/file/upload
 * <br>
 * Uploads and associates media file to specified object. This media filled can be recovered by calling GetObjectFile function.
 * <br>
 * Params: <br>
 *  - class: class name<br>
 *  - id: object identifier<br>
 *  - fn: File name<br>
 */
@Path("/objects/{class}/{id}/file/upload")
public class UploadObjectFile {
	private static Logger log = Logger.getLogger(UploadObjectFile.class);
	

	@POST
	@Produces("application/json")
	public String uploadObjectFile(@Context HttpServletRequest request, @PathParam("class") String c, @PathParam("id") String id, @QueryParam("fn") String fn) {
		try {
			InputStream in = request.getInputStream();
			return new Upload().addMediaFile(in, c+"_"+id, fn);
		} catch (Exception e) {
			log.error("Error ", e);
			return "error";
		}
	}

}
