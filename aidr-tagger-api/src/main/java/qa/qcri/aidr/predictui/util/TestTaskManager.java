package qa.qcri.aidr.predictui.util;

import java.io.Serializable;
//import java.util.Properties;



import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.core.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import qa.qcri.aidr.task.ejb.TaskManagerRemote;
import qa.qcri.aidr.task.entities.Document;



@Path("/test")
@Stateless
public class TestTaskManager {

	@Context
    private UriInfo context;
	
	@EJB
	private TaskManagerRemote<Document, Serializable> taskManager;

	public TestTaskManager() {
	}

	@SuppressWarnings("unchecked")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/remoteEJB/{id}")
	public Response test(@PathParam("id") String id) {
		StringBuilder respString = new StringBuilder().append("Fetched new doc = ");
		Long documentId = Long.parseLong(id);
		try {
			long startTime = System.currentTimeMillis();
			//Properties props = new Properties();
			//props.setProperty("java.naming.factory.initial", "com.sun.enterprise.naming.SerialInitContextFactory");
			//props.setProperty("java.naming.factory, url.pkgs", "com.sun.enterprise.naming");
			//props.setProperty("java.naming.factory.state", "com.sun.corba.ee.impl.presentation.rmi.JNDIStateFactoryImpl");
			//props.setProperty("org.omg.CORBA.ORBInitialHost", "localhost");
			//props.setProperty("org.omg.CORBA.ORBInitialPort", "3700");

			//InitialContext ctx = new InitialContext();
			//this.taskManager = (TaskManagerRemote<Document, Serializable>) ctx.lookup("java:global/aidr-task-manager/TaskManagerBean");
			Document document = taskManager.getNewTask(documentId);
			long elapsed = System.currentTimeMillis() - startTime;
			if (document != null) {
				respString.append(document.getDocumentID());
			} else {
				respString.append("null");
			}
			System.out.println("[main] " + respString.toString() + ", time taken = " + elapsed);
		} catch (Exception e) {
			System.err.println("Error in JNDI lookup");
			respString.append("Error in JNDI lookup");
			e.printStackTrace();
		}
		return Response.ok(respString.toString()).build();
	}

	/*
	public static void main(String[] args) throws Exception {
		TestTaskManager tc = new TestTaskManager(); 
		System.out.println("Result: " + tc.test().toString());
	}*/
}
