package org.fundaciotapies.ac.rest;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.fundaciotapies.ac.model.Request;

import com.google.gson.Gson;

@Path("getClassesList")
public class GetClassesList {
	
	@GET
	@Produces("application/json")
	public String getClassesList(@QueryParam("c") String parentClass) {
		List<String> classesList = null;
		if (parentClass == null)
			classesList = new Request().listObjectClasses();
		else
			classesList = new Request().listSubclasses(parentClass, false);
			classesList.add(0, parentClass);
			
		return new Gson().toJson(classesList);
	}
}