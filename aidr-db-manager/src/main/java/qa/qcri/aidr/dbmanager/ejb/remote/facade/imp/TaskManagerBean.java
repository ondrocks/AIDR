package qa.qcri.aidr.dbmanager.ejb.remote.facade.imp;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.commons.lang3.text.translate.UnicodeEscaper;
import org.apache.log4j.Logger;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import qa.qcri.aidr.common.exception.PropertyNotSetException;
import qa.qcri.aidr.dbmanager.dto.CollectionDTO;
import qa.qcri.aidr.dbmanager.dto.DocumentDTO;
import qa.qcri.aidr.dbmanager.dto.DocumentNominalLabelDTO;
import qa.qcri.aidr.dbmanager.dto.DocumentNominalLabelIdDTO;
import qa.qcri.aidr.dbmanager.dto.HumanLabeledDocumentDTO;
import qa.qcri.aidr.dbmanager.dto.ModelFamilyDTO;
import qa.qcri.aidr.dbmanager.dto.NominalAttributeDTO;
import qa.qcri.aidr.dbmanager.dto.NominalLabelDTO;
import qa.qcri.aidr.dbmanager.dto.TaskAnswerDTO;
import qa.qcri.aidr.dbmanager.dto.TaskAssignmentDTO;
import qa.qcri.aidr.dbmanager.dto.UsersDTO;
import qa.qcri.aidr.dbmanager.ejb.remote.facade.ModelFamilyResourceFacade;
import qa.qcri.aidr.dbmanager.ejb.remote.facade.TaskManagerRemote;
import qa.qcri.aidr.dbmanager.entities.misc.Collection;
import qa.qcri.aidr.dbmanager.entities.misc.Users;
import qa.qcri.aidr.dbmanager.entities.task.Document;
import qa.qcri.aidr.dbmanager.entities.task.DocumentNominalLabel;
import qa.qcri.aidr.dbmanager.entities.task.TaskAnswer;
import qa.qcri.aidr.dbmanager.entities.task.TaskAssignment;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class implements the TaskManagerRemote interface, providing the business logic for operations 
 * on the document, document_nominal_label, task_answer and task_assignment table - logically grouped as the the 'task related operations'.
 * 
 * @author Koushik
 *
 */


@Stateless
public class TaskManagerBean<T, I> implements TaskManagerRemote<T, Serializable> {

	@EJB
	private qa.qcri.aidr.dbmanager.ejb.remote.facade.CollectionResourceFacade remoteCrisisEJB;

	@EJB
	private qa.qcri.aidr.dbmanager.ejb.remote.facade.DocumentResourceFacade remoteDocumentEJB;

	@EJB
	private qa.qcri.aidr.dbmanager.ejb.remote.facade.UsersResourceFacade remoteUsersEJB;

	@EJB
	private qa.qcri.aidr.dbmanager.ejb.remote.facade.DocumentNominalLabelResourceFacade remoteDocumentNominalLabelEJB;		

	@EJB
	private qa.qcri.aidr.dbmanager.ejb.remote.facade.CrisisTypeResourceFacade remoteCrisisTypeEJB;

	@EJB
	private qa.qcri.aidr.dbmanager.ejb.remote.facade.TaskAnswerResourceFacade remoteTaskAnswerEJB;

	@EJB
	private qa.qcri.aidr.dbmanager.ejb.remote.facade.TaskAssignmentResourceFacade remoteTaskAssignmentEJB;

	@EJB
	private qa.qcri.aidr.dbmanager.ejb.remote.facade.NominalLabelResourceFacade remoteNominalLabelEJB;

	@EJB
	private ModelFamilyResourceFacade modelFamilyResourceFacade;
	protected static Logger logger = Logger.getLogger(TaskManagerBean.class);
	private static UnicodeEscaper unicodeEscaper = UnicodeEscaper.above(127); 
	
	private static Object lockObject = new Object();
	private static Integer inCS = 0;

	private Class<T> entityType;
	
	public TaskManagerBean()  {
		this.entityType = getClassType();
		//lock = new ReentrantLock();
	}  

	//private static final Object monitor = new Object();

	@SuppressWarnings("unchecked")
	public Class<T> getClassType() {
		Class<? extends Object> thisClass = getClass();
		Type genericSuperclass = thisClass.getGenericSuperclass();
		if( genericSuperclass instanceof ParameterizedType ) {
			Type[] argumentTypes = ((ParameterizedType) genericSuperclass).getActualTypeArguments();
			Class<T> entityBeanType = (Class<T>)argumentTypes[0];
			return entityBeanType;
		} else {
			return null;
		}
	}

	@Override
	public long insertNewTask(T task) {
		if (task == null) {
			logger.warn("Attempting to insert null task");
			return -1;
		}
		DocumentDTO doc = (DocumentDTO) task;
		doc.setHasHumanLabels(false);
		try {
			//documentLocalEJB.save(doc);
			DocumentDTO savedDoc = remoteDocumentEJB.addDocument(doc);
			return savedDoc.getDocumentID();
		} catch (Exception e) {
			logger.error("Error in document insertion : " + doc.getData());
		}
		return -1;
	}

	@Override
	public Long saveNewTask(T task, Long crisisID) {
		if (task == null) {
			logger.error("Attempting to insert empty task");
			return -1L;
		}
		try {
			DocumentDTO doc = (DocumentDTO) task;
			CollectionDTO crisisDTO = remoteCrisisEJB.findCrisisByID(crisisID);
			doc.setCrisisDTO(crisisDTO);
			doc.setHasHumanLabels(false);
			DocumentDTO savedDoc = remoteDocumentEJB.addDocument(doc);
			//logger.info("Saved to DB document: " + savedDoc.getDocumentID() + ", for crisis = " + savedDoc.getCrisisDTO().getCode());
			if(savedDoc != null) {
				return savedDoc.getDocumentID();
			}
		} catch (Exception e) {
			logger.error("Error in saving new document for crisisID : " + crisisID , e);
		}
		return -1L;
	}



	@Override
	public void insertNewTask(List<T> collection) {
		if (collection != null) {
			try {
				for (T doc: collection) {
					((DocumentDTO) doc).setHasHumanLabels(false);
					remoteDocumentEJB.addDocument((DocumentDTO) doc);
				}
			} catch (Exception e) {
				logger.error("Error in collection insertion");
			}
		} else {
			logger.warn("Attempting to insert NULL");
		}
	}

	@Override
	public List<Long> saveNewTasks(List<T> collection, Long crisisID) {
		List<Long> newList = new ArrayList<Long>();
		if (collection != null) {
			try {
				for (T doc: collection) {
					CollectionDTO crisisDTO = remoteCrisisEJB.findCrisisByID(crisisID);
					((DocumentDTO) doc).setCrisisDTO(crisisDTO);
					((DocumentDTO) doc).setHasHumanLabels(false);
					DocumentDTO savedDoc = remoteDocumentEJB.addDocument((DocumentDTO) doc);
					if (savedDoc != null) { 
						newList.add(savedDoc.getDocumentID());
					}
				}
			} catch (Exception e) {
				logger.error("Error in collection insertion for crisisID : " + crisisID);
			}
		} else {
			logger.warn("Attempting to insert NULL");
		}
		return newList;
	}


	@Override
	public int deleteTaskById(Long id) {
		try {
			DocumentDTO doc = remoteDocumentEJB.findDocumentByID(id);
			Integer result = remoteDocumentEJB.deleteNoLabelDocument(doc);
			return result;
		} catch(Exception e) {			
			logger.error("Error in deletion by id");
		}
		return 0;
	}

	@Override
	public int deleteTask(T task) {
		if (task != null) {
			try {
				return remoteDocumentEJB.deleteNoLabelDocument((DocumentDTO) task);
			} catch (Exception e) {
				logger.error("Error in deletion of task");
				return 0;
			}
		} else {
			logger.warn("Attempting to delete NULL");
		}
		return 0;
	}

	private List<DocumentDTO> createDocumentDTOList(List<Document> list) throws PropertyNotSetException {
		List<DocumentDTO> dtoList = new ArrayList<DocumentDTO>();
		if (list != null) {
			for (Document d : list) {
				dtoList.add(new DocumentDTO(d));
			}
		}
		return dtoList;
	}

	private List<Document> createDocumentEntityList(List<DocumentDTO> list) throws PropertyNotSetException {
		List<Document> eList = new ArrayList<Document>();
		if (list != null) {
			for (DocumentDTO d : list) {
				eList.add(d.toEntity());
			}
		}
		return eList;
	}


	@SuppressWarnings("unchecked")
	@Override
	public int deleteTask(List<T> collection) {
		List<DocumentDTO> dtoList = null;
		if (collection != null) {
			try {
				dtoList = createDocumentDTOList((List<Document>) collection);
			} catch (PropertyNotSetException e) {
				logger.error("Unable to create DTO list.", e);
			}

			try {
				return remoteDocumentEJB.deleteNoLabelDocument(dtoList);
			} catch (Exception e) {
				logger.error("Error in collection deletion of size: " + collection.size());
				return 0;
			}
		} else {
			logger.warn("Attempting to delete a NULL collection");
		}
		return 0;
	}

	@Override
	public int deleteUnassignedTask(T task) {
		if (task != null) {
			try {
				return remoteDocumentEJB.deleteUnassignedDocument((DocumentDTO) task);
			} catch (Exception e) {
				logger.error("Error in deletion");
				return 0;
			}
		} else {
			logger.warn("Attempting to delete NULL");
		}
		return 0;
	}


	@SuppressWarnings("unchecked")
	@Override
	public int deleteUnassignedTaskCollection(List<T> collection) {
		if (collection != null) {
			List<Long> idList = new ArrayList<Long>();
			for (DocumentDTO dto: (List<DocumentDTO>) collection) {
				idList.add(dto.getDocumentID());
			}
			try {
				return remoteDocumentEJB.deleteUnassignedDocumentCollection(idList);
			} catch (Exception e) {
				logger.error("Error in collection deletion");
				return 0;
			}
		} else {
			logger.warn("Attempting to delete NULL collection");
		}
		return 0;
	}


	@Override
	public int deleteStaleTasks(String joinType, String joinTable, String joinColumn,
			String sortOrder, String[] orderBy,
			final String maxTaskAge, final String scanInterval) {
		//logger.info("Received request: " + joinType + ", " + joinTable + ", " 
			//	+ joinColumn + ", " + maxTaskAge + ", " + scanInterval);
		try {
			int docDeleteCount = remoteDocumentEJB.deleteStaleDocuments(joinType, joinTable, joinColumn, 
					sortOrder, orderBy, maxTaskAge, scanInterval);
			return docDeleteCount;
		} catch (Exception e) {
			logger.warn("Error in deletion");
			return 0;
		}
	}

	@Override
	public int truncateLabelingTaskBufferForCrisis(final long crisisID, final int maxLength, final int ERROR_MARGIN) {
		List<Long> docList = null;
		try {
			docList = remoteDocumentEJB.getUnassignedDocumentIDsByCrisisID(crisisID, null);
		} catch (Exception e) {
			logger.error("Exception in fetching unassigned documents with hasHumaLabels=false");
			return 0;
		}
		DocumentDTO dto;

		// Next trim the document table for the given crisisID to the 
		// Config.LABELING_TASK_BUFFER_MAX_LENGTH size
		if (docList != null) {
			int docsToDelete = docList.size() - maxLength;
			if (docsToDelete > ERROR_MARGIN) {		// if less than this, then skip delete
				//List<Long> documentIDList = new ArrayList<Long>();
				for (int i = 0;i < docsToDelete;i++) {
					try {
						dto = remoteDocumentEJB.findDocumentByID(docList.get(i));
						remoteDocumentEJB.deleteDocument(dto);
					} catch (Exception e)
					{
						logger.error("Exception when attempting to delete document");
					}
				}
				return docsToDelete;
			} else {
				logger.debug("No need for truncation: docListSize = " + docList.size() + ", max buffer size = " + maxLength);
			}
		}
		return 0;
	}



	@Override
	public void updateTask(T task) {
		try {
			// NOTE: can't use update() since serialization+deserialization
			// of persisted entity will throw an exception. Use merge() instead.
			//documentLocalEJB.update((Document) task);
			remoteDocumentEJB.merge(((DocumentDTO) task).toEntity()); 
		} catch (Exception e) {
			logger.error("failed update");
		}
	}



	@SuppressWarnings("unchecked")
	@Override
	public void updateTaskList(List<T> collection) {
		try { 
			// NOTE: can't use update() since serialization+deserialization
			// of persisted entity will throw an exception. Use merge() instead.
			//documentLocalEJB.update((List<Document>) collection);
			remoteDocumentEJB.merge(createDocumentEntityList((List<DocumentDTO>)collection));
		} catch (Exception e) {
			logger.error("failed collection update");
		}
	}

	@Override
	public void updateTask(DocumentDTO dto) {
		try {
			// NOTE: can't use update() since serialization+deserialization
			// of persisted entity will throw an exception. Use merge() instead.
			//documentLocalEJB.update((Document) task);
			remoteDocumentEJB.merge(dto.toEntity()); 
		} catch (Exception e) {
			logger.error("failed update");
		}
	}


	/**
	 * Gets a new task from the task repository
	 * @param <entityClass>
	 */
	@Override
	public DocumentDTO getNewTask(Long crisisID) {
		try {
			return getNewTask(crisisID, null);
		} catch (Exception e) {
			logger.error("Error in fetching new task.", e);
		}
		return null;
	}

	/**
	 * Gets a new task from the task repository
	 * based on specified criterion
	 */
	@Override
	public DocumentDTO getNewTask(Long crisisID, Criterion criterion) {
		Criterion newCriterion = null;
		try {
			if (criterion != null) {
				newCriterion = Restrictions.conjunction()
						.add(criterion)
						.add(Restrictions.eq("collection.id",crisisID))
						.add(Restrictions.eq("hasHumanLabels",false));
			} else {
				newCriterion = Restrictions.conjunction()
						.add(Restrictions.eq("collection.id",crisisID))
						.add(Restrictions.eq("hasHumanLabels",false));
			}

			Document document = remoteDocumentEJB.getByCriteria(newCriterion);
			//logger.debug("New task: " + document);
			if (document != null && !isTaskAssigned(document)) {
				logger.info("New task: " + document.getDocumentId());
				return new DocumentDTO(document);
			} 
		} catch (Exception e) {
			logger.error("Error in getting new Task for crisisID: " + crisisID);
		}
		return null;
	}


	@Override
	public List<DocumentDTO> getNewTaskCollection(Long crisisID, Integer count, String order, Criterion criterion) {
		logger.debug("Received request for crisisID = " + crisisID + ", count = " + count);
		String aliasTable = "taskAssignments";
		String aliasTableKey = "taskAssignments.id.documentId";
		String[] orderBy = {"valueAsTrainingSample","documentId"};
		Criterion newCriterion = criterion;
		if (criterion != null) {
			newCriterion = Restrictions.conjunction()
					.add(criterion)
					.add(Restrictions.eq("collection.id",crisisID))
					.add(Restrictions.eq("hasHumanLabels",false));
		} else {
			newCriterion = Restrictions.conjunction()
					.add(Restrictions.eq("collection.id",crisisID))
					.add(Restrictions.eq("hasHumanLabels",false));
		}
		Criterion aliasCriterion =  (Restrictions.isNull(aliasTableKey));
		try {
			List<Document> docList = remoteDocumentEJB.getByCriteriaWithAliasByOrder(newCriterion, order, orderBy, count, aliasTable, aliasCriterion);
			if (docList != null) {
				logger.debug("[getNewTaskCollection] Fetched size = " + docList.size());
				return createDocumentDTOList(docList);
			} else {
				return null;
			}
		} catch (Exception e) {
			logger.error("Error in getting new Task collection for crisisID: " + crisisID);
		}
		return null;
	}


	@Override
	public <E> Boolean isTaskAssigned(E task) {
		List<TaskAssignmentDTO> fetchedList = null;
		if (task != null) {
			try {
				DocumentDTO document = (DocumentDTO) task;
				fetchedList= remoteTaskAssignmentEJB.findTaskAssignmentByID(document.getDocumentID());
			} catch (Exception e) {
				logger.error("Error in finding Task");
				return false;
			}
		}
		if (null == fetchedList || fetchedList.isEmpty()) {
			return false; 
		}
		return true;
	}


	@Override
	public <E> Boolean isTaskNew(E task) {
		if (task != null && !isTaskAssigned(task) && !isTaskDone(task)) {
			return true;
		} else {
			return false;
		}
	}


	@Override
	public <E> Boolean isTaskDone(E task) {
		if (task != null) {
			try {
				Document document = remoteDocumentEJB.getById(((DocumentDTO) task).getDocumentID());
				if ((document != null) && ((Document) document).isHasHumanLabels()) {
					return true;
				}
			} catch (Exception e) {
				logger.error("Error in finding document");
				return false;
			}
		}
		return false;		// no entry for documentID in task_answer table
	}


	@Override
	public <E> Boolean isExists(E task) {
		if (task != null) {
			DocumentDTO document = (DocumentDTO) task;
			try {
				if (remoteDocumentEJB.getById(document.getDocumentID()) != null) {
					return true;
				}
			} catch (Exception e) {
				logger.error("Error in finding document");
			}
		}
		return false;
	}

	@Override
	public DocumentDTO getTaskByCriterion(Long crisisID, Criterion criterion) {
		try {
			if (criterion != null) {
				Criterion newCriterion = Restrictions.conjunction()
						.add(criterion)
						.add(Restrictions.eq("collection.id", crisisID));
				Document document = remoteDocumentEJB.getByCriteria(newCriterion);

				return new DocumentDTO(document);
			}
		} catch (Exception e) {
			logger.error("Error in finding task");
		}
		return null;
	}

	@Override
	public List<DocumentDTO> getTaskCollectionByCriterion(Long crisisID, Integer count, Criterion criterion) {
		try {
			if (criterion != null) {
				Criterion newCriterion = Restrictions.conjunction()
						.add(criterion)
						.add(Restrictions.eq("collection.id", crisisID));
				List<Document> docList =  remoteDocumentEJB.getByCriteriaWithLimit(newCriterion, count);
				return createDocumentDTOList(docList);
			}
		} catch (Exception e) {
			logger.error("Error in finding task");
		}
		return null;
	}

	@Override
	public List<DocumentDTO> getNominalLabelDocumentCollection(Long nominalLabelID) {
		logger.info("Received fetch document request with nominal label = " + nominalLabelID);
		try {
			List<DocumentDTO> docList = remoteDocumentEJB.getDocumentCollectionWithNominalLabelData(nominalLabelID);
			logger.debug("docList = " + docList);
			if (docList != null) {
				//logger.info("Fetched size = " + docList.size());
				return docList;
			} 
		} catch (Exception e) {
			logger.error("Error in getting new document collection for nominal Label ID: " + nominalLabelID);
		}
		return null;
	}

	@Override
	public void taskUpdate(Criterion criterion, String joinType, String joinTable,
			String joinColumn, String sortOrder, String[] orderBy) {
		// TODO Auto-generated method stub
	}

	@Override
	public DocumentDTO getTaskById(Long id) {
		try {
			DocumentDTO document = remoteDocumentEJB.findDocumentByID(id);
			logger.debug("Fetched document: " + document);

			return document;
		} catch (Exception e) {
			logger.error("Error in finding task");
		}
		return null;
	}

	@Override
	public List<DocumentDTO> getAllTasks() {
		try {
			List<Document> docList =  remoteDocumentEJB.getAll();
			logger.debug("Fetched documents count: " + docList.size());

			return createDocumentDTOList(docList);
		} catch (Exception e) {
			logger.error("Error in finding task");
		}
		return null;
	}

	@Override
	public <E> String serializeTask(E task) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		String jsonString = null;
		try {
			if (task != null) jsonString = mapper.writeValueAsString(task);
		} catch (IOException e) {
			logger.error("JSON serialization exception");
		}
		return jsonString;
	}

	/**
	 * Example method call: deSerializeList(jsonString2, new TypeReference<List<Document>>() {}) 
	 */
	@Override
	public <E> E deSerializeList(String jsonString, TypeReference<E> type) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		try {
			if (jsonString != null) {
				E docList = mapper.readValue(jsonString, type);
				return docList;
			}	
		} catch (IOException e) {
			logger.error("JSON deserialization exception");
		}
		return null;
	}

	/**
	 * Example method call: deSerialize(jsonString, Document.class)
	 */
	@Override
	public <E> E deSerialize(String jsonString, Class<E> entityType) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		try {
			if (jsonString != null) {
				E entity = mapper.readValue(jsonString, entityType);
				return entity;
			}	
		} catch (IOException e) {
			logger.error("JSON deserialization exception");
		}
		return null;
	}

	////////////////////////////////////////////////////////////
	// Trainer API Task Assignment related APIs
	////////////////////////////////////////////////////////////
	@Override
	public List<DocumentDTO> getDocumentsForTagging(final Long crisisID, int count, final String userName, final int remainingCount) {
		UsersDTO users = null;
		List<DocumentDTO> assignList = null;
		try {
			users = remoteUsersEJB.getUserByName(userName);
		} catch (Exception e) {
			logger.error("[getDocumentsForTagging] Exception in finding user userName = " + userName + ". Aborting...");
			logger.error("Exception", e);
		}
		if (users != null) {
			int fetchedSize = 0;
				try {
					List<DocumentDTO> dtoList = getNewTaskCollection(crisisID, count, "DESC", null);
					if (dtoList != null) {
						fetchedSize = dtoList.size();
					}
					int availableRequestSize = fetchedSize - remainingCount;
					if (availableRequestSize > 0) {
						count = Math.min(count, availableRequestSize);
						if (!dtoList.isEmpty() && count > 0) {
							assignList = new ArrayList<DocumentDTO>();
							assignList.addAll(dtoList.subList(0, count));
							assignNewTaskToUser(assignList, users.getUserID());
						}
					} 
				} catch (Exception e) {
					logger.error("Exception", e);
				}
		} else {
			logger.warn("[getDocumentsForTagging] No user found with userName = " + userName + ". Aborting...");
		}
		return assignList;
	}


	@Override
	public void assignNewTaskToUser(Long id, Long userId) throws Exception {
		try {
			int retVal = remoteTaskAssignmentEJB.insertOneTaskAssignment(id, userId);
			if (retVal <= 0) {
				logger.error("unable to assign new task to user");
				throw new Exception("[assignNewTaskToUser] Couldn't do task assignment");
			}
		} catch (Exception e) {
			logger.error("Error in assignNewTaskToUser for id : " + id + " and userID : " + userId);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void assignNewTaskToUser(List<DocumentDTO> collection, Long userId) throws Exception {
		int retVal = remoteTaskAssignmentEJB.insertTaskAssignment(collection, userId);
		if (retVal <= 0) {
			logger.warn("Unable to insert task assignment");
			throw new Exception("[assignNewTaskToUser] Couldn't do task assignment");
		}
	}

	@Override
	public void undoTaskAssignment(Map<Long, Long> taskMap) throws Exception {
		int retVal = remoteTaskAssignmentEJB.undoTaskAssignment(taskMap);
		if (retVal <= 0) {
			logger.warn("Unable to undo task assignment");
			throw new Exception("[undoTaskAssignment] Couldn't undo task assignment");
		}
	}

	@Override
	public void undoTaskAssignment(Long id, Long userId) throws Exception {
		int retVal = remoteTaskAssignmentEJB.undoTaskAssignment(id, userId);
		if (retVal <= 0) {
			logger.warn("Unable to undo task assignment");
			throw new Exception("[undoTaskAssignment] Couldn't undo task assignment");
		}
	}

	@Override
	public Integer getPendingTaskCountByUser(Long userId) {
		return remoteTaskAssignmentEJB.getPendingTaskCount(userId);
	}

	@Override
	public List<TaskAssignmentDTO> getAssignedTasksById(Long id) {
		List<TaskAssignmentDTO> docList = remoteTaskAssignmentEJB.findTaskAssignmentByID(id);
		if (docList != null) {
			try {
				return docList;
			} catch (Exception e) {
				logger.error("Error in serializing collection");
			}
		}
		return null;
	}

	@Override
	public TaskAssignmentDTO getAssignedTaskByUserId(Long id, Long userId) {
		TaskAssignmentDTO assignedUserTask = remoteTaskAssignmentEJB.findTaskAssignment(id, userId);
		if (assignedUserTask != null) {
			try {
				return assignedUserTask;
			} catch (Exception e) {
				logger.error("Error in serializing collection");
			}
		}
		return null;
	}

	/**
	 * Takes as input a map consisting of the setter methods and their corresponding parameters
	 * of the entity to be modified. Returns the modified entity.
	 */
	@Override
	public <E> Object setTaskParameter(Class<E> entityType, Long id, Map<String, String> paramMap) {
		//logger.info("Received request for task ID = " + id + ", param Map = " + paramMap);
		Object doc = null;
		try {
			if (entityType.equals(qa.qcri.aidr.dbmanager.entities.task.Document.class)) {
				//logger.info("Detected of type Document.class, id = " + id);
				doc = (Document) remoteDocumentEJB.getById(id);
			} 
			if (entityType.equals(qa.qcri.aidr.dbmanager.entities.task.TaskAssignment.class)) {
				//logger.info("Detected of type TaskAssignment.class");
				doc = (TaskAssignment) remoteTaskAssignmentEJB.getById(id);
			}
			if (entityType.equals(qa.qcri.aidr.dbmanager.entities.task.TaskAnswer.class)) {
				//logger.info("Detected of type TaskAnswer.class");
				List<TaskAnswerDTO> docList = remoteTaskAnswerEJB.getTaskAnswer(id);
				if (docList != null) 
					doc = docList.get(0).toEntity();			
			}
			if (entityType.equals(qa.qcri.aidr.dbmanager.entities.misc.Users.class)) {
				//logger.info("Detected of type Users.class");
				doc = (Users) remoteUsersEJB.getById(id);
			}
			if (entityType.equals(qa.qcri.aidr.dbmanager.entities.task.DocumentNominalLabel.class)) {
				//logger.info("Detected of type DocumentNominalLabel.class");
				doc = (DocumentNominalLabel) remoteDocumentNominalLabelEJB.getById(id);
			}
			if (entityType.equals(qa.qcri.aidr.dbmanager.entities.misc.Collection.class)) {
				doc = (Collection) remoteCrisisEJB.getById(id);
			}
			//logger.info("Fetched task of type: " + doc.getClass());
			//logger.info("received map: " + paramMap);
		} catch (Exception e) {
			logger.error("Error in detecting Class Type");
			return null;
		}

		if (doc != null) {
			Class docClass = null;
			//Object obj = null;
			Method[] methods = null;
			Class[] paramTypes = null;
			try {
				//docClass = Class.forName(className);
				docClass = entityType;
				//obj = docClass.newInstance();
				methods = docClass.getDeclaredMethods();
				for (int i = 0; i < methods.length;i++) {
					//logger.info("discovered method: " + methods[i].getName());
				}
			} catch (Exception e) {
				logger.error("Error in instantiating method class");
				return null;
			}

			Iterator<String> itr = paramMap.keySet().iterator();
			while (itr.hasNext()) {
				String name = itr.next();
				try {
					int pointer = -1;
					for (int j = 0;j < methods.length;j++) {
						if (methods[j].getName().equals(name)) {
							pointer = j;
							break;
						}
					}
					paramTypes = methods[pointer].getParameterTypes(); 
					for (int j = 0; j < paramTypes.length;j++) {
						//logger.info(methods[pointer].getName() + ": discovered parameter types: " + paramTypes[j].getName());
					}
					// Convert parameter to paramType
					if (paramTypes[0].getName().toLowerCase().contains("long")) {
						methods[pointer].invoke(doc, Long.parseLong(paramMap.get(name)));
						//logger.info("Invoking with Long parameter type");
					}
					if (paramTypes[0].getName().toLowerCase().contains("int")) {
						methods[pointer].invoke(doc, Integer.parseInt(paramMap.get(name)));
						//logger.info("Invoking with Integer parameter type");
					}
					if (paramTypes[0].getName().toLowerCase().equals("boolean")) {
						methods[pointer].invoke(doc, Boolean.parseBoolean(paramMap.get(name)));
						//logger.info("Invoking with Boolean parameter type");
					}
					if (paramTypes[0].getName().equals("String")) {
						methods[pointer].invoke(doc, paramMap.get(name));
					}
				} catch (Exception e) {
					logger.error("Error in invoking method via reflection: ");
					return null;
				} 
			}	
		}

		try {
			logger.info("Will attempt to merge update with document ID = " + id);
			if (entityType.equals(qa.qcri.aidr.dbmanager.entities.task.Document.class)) {
				//logger.info("Detected of type Document.class, id = " + id);
				remoteDocumentEJB.merge((Document) doc); 
				logger.debug("Merge update successful for task id = " + id);
				//return serializeTask((qa.qcri.aidr.task.entities.Document) doc);
				return new DocumentDTO((Document) doc);
			} 
			if (entityType.equals(qa.qcri.aidr.dbmanager.entities.task.TaskAssignment.class)) {
				//logger.info("Detected of type TaskAssignment.class");
				remoteTaskAssignmentEJB.merge((TaskAssignment) doc);
				logger.debug("Merge update successful for task id = " + id);
				//return serializeTask((qa.qcri.aidr.task.entities.TaskAssignment) doc);
				return new TaskAssignmentDTO((TaskAssignment) doc);
			}
			if (entityType.equals(qa.qcri.aidr.dbmanager.entities.task.TaskAnswer.class)) {
				//logger.info("Detected of type TaskAnswer.class");
				remoteTaskAnswerEJB.merge((TaskAnswer) doc);
				logger.debug("Merge update successful for task id = " + id);
				//return serializeTask((qa.qcri.aidr.task.entities.TaskAnswer) doc);
				return new TaskAnswerDTO((TaskAnswer) doc);
			}
			if (entityType.equals(qa.qcri.aidr.dbmanager.entities.misc.Users.class)) {
				//logger.info("Detected of type Users.class");
				remoteUsersEJB.merge((Users) doc);
				logger.debug("Merge update successful for task id = " + id);
				//return serializeTask((qa.qcri.aidr.task.entities.Users) doc);
				return new UsersDTO((Users) doc);
			}
			if (entityType.equals(qa.qcri.aidr.dbmanager.entities.task.DocumentNominalLabel.class)) {
				//logger.info("Detected of type DocumentNominalLabel.class");
				remoteDocumentNominalLabelEJB.merge((DocumentNominalLabel) doc);
				logger.debug("Merge update successful for task id = " + id);
				//return serializeTask((qa.qcri.aidr.task.entities.DocumentNominalLabel) doc);
				return new DocumentNominalLabelDTO((DocumentNominalLabel) doc);
			}
			if (entityType.equals(qa.qcri.aidr.dbmanager.entities.misc.Collection.class)) {
				remoteCrisisEJB.merge((Collection) doc);
				logger.debug("Merge update successful for task id = " + id);
				//return serializeTask((qa.qcri.aidr.task.entities.Crisis) doc);
				return new CollectionDTO((Collection) doc);
			}
		} catch (Exception e) {
			logger.error("Error in updating entity on DB");
		}
		return null;
	}

	@Override
	public void insertTaskAnswer(TaskAnswerDTO taskAnswer) {
		try {
			remoteTaskAnswerEJB.insertTaskAnswer(taskAnswer);
		} catch (Exception e) {
			logger.error("Error in saving task answer : " + taskAnswer.getDocumentID() + ", " + taskAnswer.getAnswer() + ", " + taskAnswer.getUserID());
		}
	}




	////////////////////////////////////////////
	// User service related APIs
	////////////////////////////////////////////
	@Override
	public UsersDTO getUserByName(String name) {
		UsersDTO user = null;
		try {
			user = remoteUsersEJB.getUserByName(name);
		} catch (PropertyNotSetException e) {
			logger.error("Error in getUserByName for name : " + name, e);
		}
		return user;
	}

	@Override
	public UsersDTO getUserById(Long id) {
		UsersDTO user = null;
		try {
			user = remoteUsersEJB.getUserById(id);
		} catch (PropertyNotSetException e) {
			logger.error("Error in getUserById for id : " + id, e);
		}
		//return serializeTask(user);
		return user;
	}

	@Override
	public List<UsersDTO> getAllUserByName(String name) {
		List<UsersDTO> userList = new ArrayList<UsersDTO>();
		try {
			userList = remoteUsersEJB.getAllUsersByName(name);
		} catch (PropertyNotSetException e) {
			logger.error("Error in getAllUserByName for name : " + name, e);
		}
		return userList;
	}


	////////////////////////??////////////////////
	// DocumentNominalLabel service related APIs
	//////////////////////////??//////////////////

	@Override
	public void saveDocumentNominalLabel(DocumentNominalLabelDTO documentNominalLabel) {
		try {
			DocumentNominalLabelDTO dto = remoteDocumentNominalLabelEJB.addDocument(documentNominalLabel);
			logger.info("Saved to DB document nominal label: " + dto.getIdDTO().getDocumentId() + ", with nominal label = " + dto.getIdDTO().getNominalLabelId());
		} catch (Exception e) {
			logger.error("Error in saving document nominal label : " + documentNominalLabel, e);
		}
	}

	@Override
	public boolean foundDuplicateDocumentNominalLabel(DocumentNominalLabelDTO documentNominalLabel) {
		Map<String, Long> attMap = new HashMap<String, Long>();
		try {
			attMap.put("id.documentId", documentNominalLabel.getIdDTO().getDocumentId());
			attMap.put("id.nominalLabelId", documentNominalLabel.getIdDTO().getNominalLabelId());
		} catch (PropertyNotSetException e) {
			logger.warn("Warning! duplication nominal label");
		}
		DocumentNominalLabel obj =  remoteDocumentNominalLabelEJB.getByCriterionID(Restrictions.allEq(attMap));

		if(obj != null) {
			return true;
		} else {
			return false;  //To change body of implemented methods use File | Settings | File Templates.
		}
	}

	@Override
	public DocumentDTO getDocumentById(Long id) {
		try {
			Document document = remoteDocumentEJB.getById(id);
			logger.debug("Fetched document: " + document);

			return new DocumentDTO(document);
		} catch (Exception e) {
			logger.error("Error in finding task");
		}
		return null;
	}

	@Override
	public String pingRemoteEJB() {
		StringBuilder sb = new StringBuilder("{\"status\": \"RUNNING\"}");
		return sb.toString();
	}

	@Override
	public List<HumanLabeledDocumentDTO> getHumanLabeledDocumentsByCrisisID(Long crisisID, Integer count) throws Exception {
		if (null == crisisID) {
			logger.error("crisisID can't be null");
			throw new PropertyNotSetException("crisisID can't be null");
		}
		logger.debug("Received request for crisisID = " + crisisID + ", count = " + count);
		List <HumanLabeledDocumentDTO> labeledDocList = null;

		String aliasTable = "documentNominalLabels";
		String aliasTableKeyField = "documentNominalLabels.id.nominalLabelId";
		String[] orderBy = {"documentId"};

		Criterion criterion = Restrictions.conjunction()
				.add(Restrictions.eq("collection.id",crisisID))
				.add(Restrictions.eq("hasHumanLabels", true));
		Criterion aliasCriterion =  Restrictions.isNotNull(aliasTableKeyField);
		try {
			List<Document> docList = remoteDocumentEJB.getByCriteriaWithInnerJoinByOrder(criterion, "DESC", orderBy, count, aliasTable, aliasCriterion);
			if (docList != null) {
				Set<Document> docSet = new TreeSet<Document>(new DocumentComparator());
				docSet.addAll(docList);

				// First get all labels for the fetched documents
				labeledDocList = new ArrayList<HumanLabeledDocumentDTO>();
				for (Document doc: docSet) {
					List<DocumentNominalLabelDTO> labeledDataDTO = remoteDocumentNominalLabelEJB.findLabeledDocumentListByID(doc.getDocumentId());
					if (labeledDataDTO != null) {
						for (DocumentNominalLabelDTO dto: labeledDataDTO) {
							NominalLabelDTO nominalLabel = remoteNominalLabelEJB.getNominalLabelWithAllFieldsByID(dto.getIdDTO().getNominalLabelId());
							if (nominalLabel != null) {
								nominalLabel.setDocumentNominalLabelsDTO(null);
								nominalLabel.setModelNominalLabelsDTO(null);
							}
							dto.setNominalLabelDTO(nominalLabel);
							dto.setDocumentDTO(null);
						}
						labeledDocList.add(new HumanLabeledDocumentDTO(new DocumentDTO(doc), labeledDataDTO));
					}
				}
				return labeledDocList;
			} else {
				return null;
			}
		} catch (Exception e) {
			logger.error("Error in getting human labeled documents collection for crisisID: " + crisisID);
			logger.error("exception", e);
		}
		return null;
	}

	@Override
	public List<HumanLabeledDocumentDTO> getHumanLabeledDocumentsByCrisisCode(String crisisCode, Integer count) throws Exception{
		if (null == crisisCode) {
			logger.error("crisis code can't be null");
			throw new PropertyNotSetException("crisis code can't be null");
		}
		CollectionDTO crisis = remoteCrisisEJB.getCrisisByCode(crisisCode);
		if (null == crisis) {
			logger.error("crisis code is invalid");
			throw new PropertyNotSetException("crisis code is invalid");
		}
		//logger.info("Received request for crisis code = " + crisisCode + ", count = " + count);
		return this.getHumanLabeledDocumentsByCrisisID(crisis.getCrisisID(), count);
	}

	@Override
	public List<HumanLabeledDocumentDTO> getHumanLabeledDocumentsByCrisisIDUserID(Long crisisID, Long userID, Integer count) throws Exception {
		if (null == crisisID || null == userID) {
			logger.error("crisis ID or userID can't be null");
			throw new PropertyNotSetException("crisis ID or userID can't be null");
		}
		//logger.info("Received request for crisisID = " + crisisID + ", userID = " + userID + ", count = " + count);
		List <HumanLabeledDocumentDTO> labeledDocList = null;

		String aliasTable = "documentNominalLabels";
		String aliasTableKeyField = "documentNominalLabels.id.nominalLabelId";
		String[] orderBy = {"documentId"};

		Criterion criterion = Restrictions.conjunction()
				.add(Restrictions.eq("collection.id",crisisID))
				.add(Restrictions.eq("hasHumanLabels", true));
		Criterion aliasCriterion =  Restrictions.conjunction()
				.add(Restrictions.isNotNull(aliasTableKeyField))
				.add(Restrictions.eq("documentNominalLabels.id.userId", userID));
		try {
			List<Document> docList = remoteDocumentEJB.getByCriteriaWithInnerJoinByOrder(criterion, "DESC", orderBy, count, aliasTable, aliasCriterion);
			if (docList != null) {
				Set<Document> docSet = new TreeSet<Document>(new DocumentComparator());
				docSet.addAll(docList);

				// First get all labels for the fetched documents
				labeledDocList = new ArrayList<HumanLabeledDocumentDTO>();
				for (Document doc: docSet) {
					List<DocumentNominalLabelDTO> labeledDataDTO = remoteDocumentNominalLabelEJB.findLabeledDocumentListByID(doc.getDocumentId());
					if (labeledDataDTO != null) {
						for (DocumentNominalLabelDTO dto: labeledDataDTO) {
							NominalLabelDTO nominalLabel = remoteNominalLabelEJB.getNominalLabelWithAllFieldsByID(dto.getIdDTO().getNominalLabelId());
							if (nominalLabel != null) {
								nominalLabel.setDocumentNominalLabelsDTO(null);
								nominalLabel.setModelNominalLabelsDTO(null);
							}
							dto.setNominalLabelDTO(nominalLabel);
							dto.setDocumentDTO(null);
						}
						labeledDocList.add(new HumanLabeledDocumentDTO(new DocumentDTO(doc), labeledDataDTO));
					}
				}
				return labeledDocList;
			} else {
				return null;
			}
		} catch (Exception e) {
			logger.error("Error in getting human labeled documents collection for crisisID: " + crisisID);
			logger.error("exception", e);
		}
		return null;

	}

	@Override
	public List<HumanLabeledDocumentDTO> getHumanLabeledDocumentsByCrisisIDUserName(Long crisisID, String userName, Integer count) throws Exception {
		if (null == crisisID || null == userName) {
			logger.error("crisis ID or userName can't be null");
			throw new PropertyNotSetException("crisis ID or userName can't be null");
		}
		UsersDTO user = remoteUsersEJB.getUserByName(userName);
		if (null == user) {
			logger.error("User name is invalid");
			throw new PropertyNotSetException("User name is invalid");
		}
		//logger.info("Received request for crisisID = " + crisisID + ", userName = " + userName + ", count = " + count);
		return this.getHumanLabeledDocumentsByCrisisIDUserID(crisisID, user.getUserID(), count);
	}

	@Override
	public boolean deleteTask(Long crisisID, Long userID) {

		boolean success;
		try {
			List<DocumentDTO> documentDTOs = remoteDocumentEJB.findDocumentsByCrisisID(crisisID);
			
			if(documentDTOs != null && userID != null) {
				for(DocumentDTO documentDTO : documentDTOs) {
					remoteTaskAssignmentEJB.undoTaskAssignment(documentDTO.getDocumentID(), userID);
					remoteTaskAnswerEJB.deleteTaskAnswer(documentDTO.getDocumentID());
				}
			}
			remoteDocumentEJB.deleteDocuments(documentDTOs);
			success = true;
			
			//logger.info("Successful deletion for task data.");
		} catch (Exception e) {
			logger.error("Unable to delete task for crisidID : " + crisisID + " and userID : " + userID);
			success = false;
		}
		
		return success;
	}

	@Override
	@Asynchronous
	public void importTrainingDataForClassifier(Long targetCollectionId, Long sourceCollectionId, Long nominalAttributeId) {
		try {
			
			List<Long> nominalLabelIds = remoteNominalLabelEJB.getNominalLabelIdsByAttributeID(nominalAttributeId);
			List<DocumentDTO> documentDTOs = remoteDocumentEJB.getDocumentForNominalLabelAndCrisis(nominalLabelIds, sourceCollectionId);
			
			CollectionDTO collectionDTO = remoteCrisisEJB.findCrisisByID(targetCollectionId);
			CollectionDTO sourceCollection = remoteCrisisEJB.findCrisisByID(sourceCollectionId);
			
			// save model family
			ModelFamilyDTO modelFamilyDTO = new ModelFamilyDTO();
			modelFamilyDTO.setCrisisDTO(collectionDTO);
			NominalAttributeDTO attributeDTO = new NominalAttributeDTO();
			attributeDTO.setNominalAttributeId(nominalAttributeId);
			modelFamilyDTO.setNominalAttributeDTO(attributeDTO);
			modelFamilyDTO.setIsActive(true);
			boolean success = modelFamilyResourceFacade.addCrisisAttribute(modelFamilyDTO);
			
			if(success) {
				
				// iterate through each tagged document 
				for(DocumentDTO documentDTO : documentDTOs) {
					DocumentDTO documentToSave = new DocumentDTO();
					documentToSave.setCrisisDTO(collectionDTO);
					documentToSave.setData(UnicodeEscaper.outsideOf(32, 0x7f).translate(documentDTO.getData()));
					documentToSave.setGeoFeatures(documentDTO.getGeoFeatures());
					documentToSave.setDoctype(documentDTO.getDoctype());
					documentToSave.setHasHumanLabels(true);
					documentToSave.setLanguage(documentDTO.getLanguage());
					documentToSave.setWordFeatures(UnicodeEscaper.outsideOf(32, 0x7f).translate(documentDTO.getWordFeatures()));
					documentToSave.setValueAsTrainingSample(documentDTO.getValueAsTrainingSample());
					documentToSave.setIsEvaluationSet(documentDTO.getIsEvaluationSet());
					documentToSave.setReceivedAt(documentDTO.getReceivedAt());
					documentToSave.setSourceCollection(sourceCollection);
					
					// save new document
					DocumentDTO newDocument = remoteDocumentEJB.addDocument(documentToSave);
					
					// fetch document nominal label for existing doc
					List<DocumentNominalLabelDTO> documentNominalLabelDTOs = remoteDocumentNominalLabelEJB.findLabeledDocumentListByID(documentDTO.getDocumentID());
					
					// add new document labels
					if(documentNominalLabelDTOs != null) {
						for(DocumentNominalLabelDTO documentNominalLabelDTO : documentNominalLabelDTOs) {
							DocumentNominalLabelDTO labelDTOToSave = new DocumentNominalLabelDTO();
							labelDTOToSave.setDocumentDTO(newDocument);
							labelDTOToSave.setNominalLabelDTO(documentNominalLabelDTO.getNominalLabelDTO());
							labelDTOToSave.setIdDTO(new DocumentNominalLabelIdDTO(newDocument.getDocumentID(), 
											documentNominalLabelDTO.getIdDTO().getNominalLabelId(), documentNominalLabelDTO.getIdDTO().getUserId()));
							this.saveDocumentNominalLabel(labelDTOToSave);
						}
					}
					
					// fetch task answers for existing doc
					List<TaskAnswerDTO> answers = remoteTaskAnswerEJB.getTaskAnswer(documentDTO.getDocumentID());
					
					// save task answers 
					for(TaskAnswerDTO answer : answers) {
						TaskAnswerDTO answerToSave = new TaskAnswerDTO();
						answerToSave.setAnswer(answer.getAnswer());
						answerToSave.setDocumentID(newDocument.getDocumentID());
						
						answerToSave.setUserID(answer.getUserID());
						answerToSave.setTimestamp(new Date());
						remoteTaskAnswerEJB.insertTaskAnswer(answerToSave);
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error in importing training data for collection id : " + sourceCollectionId +
					" and attribute : " + nominalAttributeId, e);
			
		}
	}
	
	private class DocumentComparator implements Comparator<Document> {

		@Override
		public int compare(Document d1, Document d2) {
			return d1.getDocumentId().compareTo(d2.getDocumentId());
		}

	}
}
