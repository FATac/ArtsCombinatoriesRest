package org.fundaciotapies.ac.rest;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.fundaciotapies.ac.model.Request;
import org.fundaciotapies.ac.model.Upload;
import org.fundaciotapies.ac.model.support.CustomMap;

import com.google.gson.Gson;

@Path("replaceUri")
public class ReplaceUriField {
	
	@GET
	@Produces("application/json")
	public String replaceUriField(@QueryParam("uriField") String uriField, @QueryParam("oldUri") String oldUri, @QueryParam("newUri") String newUri) {
		
		Request request = new Request();
		List<String> ids = request.specificObjectSearch(uriField, oldUri, null);
		Upload upload = new Upload();
		for (String id : ids) {
			
			CustomMap obj = request.getObject(id, "");
			Object uri = obj.get(uriField);
			String[] uris = null;
			if (uri instanceof String) {
				uris = new String[]{ (String)uri };
			} else {
				uris = (String[])uri;
			}
			
			String[] newFields = new String[uris.length];
			String[] newUris = new String[uris.length];
			for(int i=0; i<uris.length; i++) {
				newFields[i] = uriField;
				newUris[i] = uris[i].replaceAll(oldUri, newUri);
			}
			
			String result = upload.updateObject(id, newFields, newUris);
			if ("error".equals(result)) return new Gson().toJson("error");
		}
		
		return new Gson().toJson("success");
	}

}
