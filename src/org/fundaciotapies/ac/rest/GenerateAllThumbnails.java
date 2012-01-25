package org.fundaciotapies.ac.rest;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.fundaciotapies.ac.model.Request;
import org.fundaciotapies.ac.view.ViewGenerator;

import com.google.gson.Gson;

@Path("/generateAllThumbnails")
public class GenerateAllThumbnails {
	
	@GET
	@Produces("application/json")
	public String generate(@QueryParam("c") String c) {
		try {
			List<String> idList = new Request().listObjectsId(c);
			ViewGenerator view = new ViewGenerator();
			for (String id : idList) {
				view.getObjectThumbnail(id, "", true);
			}
			return new Gson().toJson("success");
		} catch (Exception e) {
			return new Gson().toJson("error");
		}
	}
}
