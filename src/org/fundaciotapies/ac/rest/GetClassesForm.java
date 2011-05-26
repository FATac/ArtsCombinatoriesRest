package org.fundaciotapies.ac.rest;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.fundaciotapies.ac.model.Request;
import org.fundaciotapies.ac.view.UploadDataFormFactory;

import com.google.gson.Gson;

@Path("getClassesForm")
public class GetClassesForm {
	
	@GET
	@Produces("application/json")
	public String getClassesForm(@QueryParam("c") String parentClass) {
		List<String> classesList = null;
		if (parentClass == null)
			classesList = new Request().listObjectClasses();
		else
			classesList = new Request().listAllSubclasses(parentClass);
			classesList.add(0, parentClass);
			
		return new Gson().toJson(UploadDataFormFactory.pickClassForm(classesList));
	}
}