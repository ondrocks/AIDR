
package qa.qcri.aidr.analysis.facade.imp;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qa.qcri.aidr.analysis.entity.ConfidenceData;
import qa.qcri.aidr.analysis.entity.ConfidenceDataPK;
import qa.qcri.aidr.analysis.facade.ConfidenceStatisticsResourceFacade;

import qa.qcri.aidr.common.values.ReturnCode;
/**
 * This class is not used at the moment.
 */

@Stateless(name = "ConfidenceStatisticsResourceFacadeImp")
public class ConfidenceStatisticsResourceFacadeImp implements ConfidenceStatisticsResourceFacade {
	
	private static Logger logger = LoggerFactory.getLogger(ConfidenceStatisticsResourceFacadeImp.class);
	
	@PersistenceContext(unitName = "qa.qcri.aidr.analysis-EJBS")
	private EntityManager em;
	
	@Override
	public EntityManager getEntityManager() {
		try {
			return em; 
		} catch (Exception e) {
			throw new HibernateException("getEntityManager failed");
		}
	}

	@Override
	public int setEntityManager(EntityManager em) {
		try {
			if (null == this.em) { 
				this.em = em;
				logger.info("EntityManager set to new value: " + this.em);
				return 1;
			} else 
				logger.info("Skipping setter, since EntityManager already initialized to :" + this.em);
			return 0;
		} catch (Exception e) {
			logger.error("EntityManager setting exception : " + em);
			logger.error("exception", e);
			throw new HibernateException("setEntityManager failed");
		}
	}

	@Override
	public Session getCurrentSession() {
		try { 
			return em.unwrap(Session.class);
		} catch (Exception e) {
			logger.error("exception: ", e);
			e.printStackTrace();
			throw new HibernateException("getCurrentSession failed");
		}
	}
	
	
	@Override
	public ReturnCode writeData(ConfidenceData confData) {
		try {
			em.persist(confData);
			return ReturnCode.SUCCESS;
		} catch (Exception e) {
			logger.error("Failure in persisting conf data for: " + confData.getCrisisCode() + ", " + confData.getAttributeCode() 
					+ ", " + confData.getLabelCode() + ", " + confData.getTimestamp() + ", " + confData.getGranularity() 
					+ ", " + confData.getBin() + ": " + confData.getCount(), e);
			return ReturnCode.ERROR;
		}
	}

	@Override
	public ConfidenceData getSingleDataByPK(ConfidenceDataPK confDataPK) {
		Criteria criteria = getCurrentSession().createCriteria(ConfidenceData.class);
		Criterion criterion = Restrictions.conjunction()
				.add(Restrictions.eq("crisisCode", confDataPK.getCrisisCode()))
				.add(Restrictions.eq("timestamp", confDataPK.getTimestamp()))
				.add(Restrictions.eq("granularity", confDataPK.getGranularity()))
				.add(Restrictions.eq("attributeCode", confDataPK.getAttributeCode()))
				.add(Restrictions.eq("labelCode", confDataPK.getLabelCode()))
				.add(Restrictions.eq("bin", confDataPK.getBin()));
		criteria.add(criterion); 		
		try {
			ConfidenceData obj = (ConfidenceData) criteria.uniqueResult();
			return obj;
		} catch (HibernateException e) {
			logger.error("exception", e);
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public List<ConfidenceData> getDataByCrisis(String crisisCode) {
		Criteria criteria = getCurrentSession().createCriteria(ConfidenceData.class);
		criteria.add(Restrictions.eq("crisisCode", crisisCode)); 		
		try {
			List<ConfidenceData> objList = (List<ConfidenceData>) criteria.list();
			return objList;
		} catch (HibernateException e) {
			logger.error("exception", e);
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<ConfidenceData> getDataByCrisisAttributeLabel(String crisisCode, String attributeCode, String labelCode) {
		Criteria criteria = getCurrentSession().createCriteria(ConfidenceData.class);
		Criterion criterion = Restrictions.conjunction()
				.add(Restrictions.eq("crisisCode", crisisCode))
				.add(Restrictions.eq("attributeCode", attributeCode))
				.add(Restrictions.eq("labelCode", labelCode));
		criteria.add(criterion); 		
		try {
			List<ConfidenceData> objList = (List<ConfidenceData>) criteria.list();
			return objList;
		} catch (HibernateException e) {
			logger.error("exception", e);
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<ConfidenceData> getDataByCrisisAttributeLabelGranularity(String crisisCode, String attributeCode, String labelCode, Long granularity) {
		Criteria criteria = getCurrentSession().createCriteria(ConfidenceData.class);
		Criterion criterion = Restrictions.conjunction()
				.add(Restrictions.eq("crisisCode", crisisCode))
				.add(Restrictions.eq("attributeCode", attributeCode))
				.add(Restrictions.eq("labelCode", labelCode))
				.add(Restrictions.eq("granularity", granularity));
		criteria.add(criterion); 		
		try {
			List<ConfidenceData> objList = (List<ConfidenceData>) criteria.list();
			return objList;
		} catch (HibernateException e) {
			logger.error("exception", e);
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<ConfidenceData> getDataAfterTimestamp(String crisisCode, String attributeCode, String labelCode, Long timestamp) {
		Criteria criteria = getCurrentSession().createCriteria(ConfidenceData.class);
		Criterion criterion = Restrictions.conjunction()
				.add(Restrictions.eq("crisisCode", crisisCode))
				.add(Restrictions.eq("attributeCode", attributeCode))
				.add(Restrictions.eq("labelCode", labelCode))
				.add(Restrictions.ge("timestamp", timestamp));
				
		criteria.add(criterion); 		
		try {
			List<ConfidenceData> objList = (List<ConfidenceData>) criteria.list();
			return objList;
		} catch (HibernateException e) {
			logger.error("exception", e);
			e.printStackTrace();
		}
		return null;	
	}

	@Override
	public List<ConfidenceData> getDataAfterTimestampGranularity(String crisisCode, String attributeCode, String labelCode,
																Long timestamp, Long granularity) {
		Criteria criteria = getCurrentSession().createCriteria(ConfidenceData.class);
		Criterion criterion = Restrictions.conjunction()
				.add(Restrictions.eq("crisisCode", crisisCode))
				.add(Restrictions.eq("attributeCode", attributeCode))
				.add(Restrictions.eq("labelCode", labelCode))
				.add(Restrictions.ge("timestamp", timestamp))
				.add(Restrictions.eq("granularity", granularity));
				
		criteria.add(criterion); 		
		try {
			List<ConfidenceData> objList = (List<ConfidenceData>) criteria.list();
			return objList;
		} catch (HibernateException e) {
			logger.error("exception", e);
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<ConfidenceData> getDataBeforeTimestamp(String crisisCode, String attributeCode, String labelCode, Long timestamp) {
		Criteria criteria = getCurrentSession().createCriteria(ConfidenceData.class);
		Criterion criterion = Restrictions.conjunction()
				.add(Restrictions.eq("crisisCode", crisisCode))
				.add(Restrictions.eq("attributeCode", attributeCode))
				.add(Restrictions.eq("labelCode", labelCode))
				.add(Restrictions.le("timestamp", timestamp));
				
		criteria.add(criterion); 		
		try {
			List<ConfidenceData> objList = (List<ConfidenceData>) criteria.list();
			return objList;
		} catch (HibernateException e) {
			logger.error("exception", e);
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<ConfidenceData> getDataBeforeTimestampGranularity(String crisisCode, String attributeCode, String labelCode,
																	Long timestamp, Long granularity) {
		Criteria criteria = getCurrentSession().createCriteria(ConfidenceData.class);
		Criterion criterion = Restrictions.conjunction()
				.add(Restrictions.eq("crisisCode", crisisCode))
				.add(Restrictions.eq("attributeCode", attributeCode))
				.add(Restrictions.eq("labelCode", labelCode))
				.add(Restrictions.le("timestamp", timestamp))
				.add(Restrictions.eq("granularity", granularity));
				
		criteria.add(criterion); 		
		try {
			List<ConfidenceData> objList = (List<ConfidenceData>) criteria.list();
			return objList;
		} catch (HibernateException e) {
			logger.error("exception", e);
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<ConfidenceData> getDataInInterval(String crisisCode, String attributeCode, String labelCode, Long timestamp1, Long timestamp2) {
		Criteria criteria = getCurrentSession().createCriteria(ConfidenceData.class);
		Criterion criterion = Restrictions.conjunction()
				.add(Restrictions.eq("crisisCode", crisisCode))
				.add(Restrictions.eq("attributeCode", attributeCode))
				.add(Restrictions.eq("labelCode", labelCode))
				.add(Restrictions.ge("timestamp", timestamp1))
				.add(Restrictions.le("timestamp", timestamp2));
				
		criteria.add(criterion); 		
		try {
			List<ConfidenceData> objList = (List<ConfidenceData>) criteria.list();
			return objList;
		} catch (HibernateException e) {
			logger.error("exception", e);
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<ConfidenceData> getDataInIntervalWithGranularity(String crisisCode, String attributeCode, String labelCode, 
																	Long timestamp1, Long timestamp2, Long granularity) {
		Criteria criteria = getCurrentSession().createCriteria(ConfidenceData.class);
		Criterion criterion = Restrictions.conjunction()
				.add(Restrictions.eq("crisisCode", crisisCode))
				.add(Restrictions.eq("attributeCode", attributeCode))
				.add(Restrictions.eq("labelCode", labelCode))
				.add(Restrictions.eq("granularity", granularity))
				.add(Restrictions.ge("timestamp", timestamp1))
				.add(Restrictions.le("timestamp", timestamp2));
				
		criteria.add(criterion); 		
		try {
			List<ConfidenceData> objList = (List<ConfidenceData>) criteria.list();
			return objList;
		} catch (HibernateException e) {
			logger.error("exception", e);
			e.printStackTrace();
		}
		return null;
	}

	
	////////////////////////////////////////////////
	// 				Bin related
	///////////////////////////////////////////////
	
	@Override
	public List<ConfidenceData> getDataByCrisisWithBin(String crisisCode, Integer bin) {
		Criteria criteria = getCurrentSession().createCriteria(ConfidenceData.class);
		Criterion criterion = Restrictions.conjunction()
				.add(Restrictions.eq("crisisCode", crisisCode))
				.add(Restrictions.ge("bin", bin));
		criteria.add(criterion); 
		try {
			List<ConfidenceData> objList = (List<ConfidenceData>) criteria.list();
			return objList;
		} catch (HibernateException e) {
			logger.error("exception", e);
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<ConfidenceData> getDataByCrisisAttributeLabelWithBin(String crisisCode, String attributeCode, String labelCode,
																		Integer bin) {
		Criteria criteria = getCurrentSession().createCriteria(ConfidenceData.class);
		Criterion criterion = Restrictions.conjunction()
				.add(Restrictions.eq("crisisCode", crisisCode))
				.add(Restrictions.eq("attributeCode", attributeCode))
				.add(Restrictions.eq("labelCode", labelCode))
				.add(Restrictions.ge("bin", bin));
		criteria.add(criterion); 		
		try {
			List<ConfidenceData> objList = (List<ConfidenceData>) criteria.list();
			return objList;
		} catch (HibernateException e) {
			logger.error("exception", e);
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<ConfidenceData> getDataByCrisisAttributeLabelGranularityWithBin(String crisisCode, String attributeCode, String labelCode,
																		Long granularity, Integer bin) {
		Criteria criteria = getCurrentSession().createCriteria(ConfidenceData.class);
		Criterion criterion = Restrictions.conjunction()
				.add(Restrictions.eq("crisisCode", crisisCode))
				.add(Restrictions.eq("attributeCode", attributeCode))
				.add(Restrictions.eq("labelCode", labelCode))
				.add(Restrictions.eq("granularity", granularity))
				.add(Restrictions.ge("bin", bin));
		criteria.add(criterion); 		
		try {
			List<ConfidenceData> objList = (List<ConfidenceData>) criteria.list();
			return objList;
		} catch (HibernateException e) {
			logger.error("exception", e);
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<ConfidenceData> getDataAfterTimestampWithBin(String crisisCode, String attributeCode, String labelCode, 
																		Long timestamp, Integer bin) {
		Criteria criteria = getCurrentSession().createCriteria(ConfidenceData.class);
		Criterion criterion = Restrictions.conjunction()
				.add(Restrictions.eq("crisisCode", crisisCode))
				.add(Restrictions.eq("attributeCode", attributeCode))
				.add(Restrictions.eq("labelCode", labelCode))
				.add(Restrictions.ge("timestamp", timestamp))
				.add(Restrictions.ge("bin", bin));
				
		criteria.add(criterion); 		
		try {
			List<ConfidenceData> objList = (List<ConfidenceData>) criteria.list();
			return objList;
		} catch (HibernateException e) {
			logger.error("exception", e);
			e.printStackTrace();
		}
		return null;	
	}

	@Override
	public List<ConfidenceData> getDataAfterTimestampGranularityWithBin(String crisisCode, String attributeCode, String labelCode,
																		Long timestamp, Long granularity, Integer bin) {
		Criteria criteria = getCurrentSession().createCriteria(ConfidenceData.class);
		Criterion criterion = Restrictions.conjunction()
				.add(Restrictions.eq("crisisCode", crisisCode))
				.add(Restrictions.eq("attributeCode", attributeCode))
				.add(Restrictions.eq("labelCode", labelCode))
				.add(Restrictions.ge("timestamp", timestamp))
				.add(Restrictions.eq("granularity", granularity))
				.add(Restrictions.ge("bin", bin));
				
		criteria.add(criterion); 		
		try {
			List<ConfidenceData> objList = (List<ConfidenceData>) criteria.list();
			return objList;
		} catch (HibernateException e) {
			logger.error("exception", e);
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<ConfidenceData> getDataBeforeTimestampWithBin(String crisisCode, String attributeCode, String labelCode, 
																		Long timestamp, Integer bin) {
		Criteria criteria = getCurrentSession().createCriteria(ConfidenceData.class);
		Criterion criterion = Restrictions.conjunction()
				.add(Restrictions.eq("crisisCode", crisisCode))
				.add(Restrictions.eq("attributeCode", attributeCode))
				.add(Restrictions.eq("labelCode", labelCode))
				.add(Restrictions.le("timestamp", timestamp))
				.add(Restrictions.ge("bin", bin));
				
		criteria.add(criterion); 		
		try {
			List<ConfidenceData> objList = (List<ConfidenceData>) criteria.list();
			return objList;
		} catch (HibernateException e) {
			logger.error("exception", e);
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<ConfidenceData> getDataBeforeTimestampGranularityWithBin(String crisisCode, String attributeCode, String labelCode,
																		Long timestamp, Long granularity, Integer bin) {
		Criteria criteria = getCurrentSession().createCriteria(ConfidenceData.class);
		Criterion criterion = Restrictions.conjunction()
				.add(Restrictions.eq("crisisCode", crisisCode))
				.add(Restrictions.eq("attributeCode", attributeCode))
				.add(Restrictions.eq("labelCode", labelCode))
				.add(Restrictions.le("timestamp", timestamp))
				.add(Restrictions.eq("granularity", granularity))
				.add(Restrictions.ge("bin", bin));
				
		criteria.add(criterion); 		
		try {
			List<ConfidenceData> objList = (List<ConfidenceData>) criteria.list();
			return objList;
		} catch (HibernateException e) {
			logger.error("exception", e);
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<ConfidenceData> getDataInIntervalWithBin(String crisisCode, String attributeCode, String labelCode, Long timestamp1,
																		Long timestamp2, Integer bin) {
		Criteria criteria = getCurrentSession().createCriteria(ConfidenceData.class);
		Criterion criterion = Restrictions.conjunction()
				.add(Restrictions.eq("crisisCode", crisisCode))
				.add(Restrictions.eq("attributeCode", attributeCode))
				.add(Restrictions.eq("labelCode", labelCode))
				.add(Restrictions.ge("timestamp", timestamp1))
				.add(Restrictions.le("timestamp", timestamp2))
				.add(Restrictions.ge("bin", bin));
				
		criteria.add(criterion); 		
		try {
			List<ConfidenceData> objList = (List<ConfidenceData>) criteria.list();
			return objList;
		} catch (HibernateException e) {
			logger.error("exception", e);
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<ConfidenceData> getDataInIntervalWithGranularityWithBin(String crisisCode, String attributeCode, String labelCode,
																		Long timestamp1, Long timestamp2, Long granularity, Integer bin) {
		Criteria criteria = getCurrentSession().createCriteria(ConfidenceData.class);
		Criterion criterion = Restrictions.conjunction()
				.add(Restrictions.eq("crisisCode", crisisCode))
				.add(Restrictions.eq("attributeCode", attributeCode))
				.add(Restrictions.eq("labelCode", labelCode))
				.add(Restrictions.eq("granularity", granularity))
				.add(Restrictions.ge("timestamp", timestamp1))
				.add(Restrictions.le("timestamp", timestamp2))
				.add(Restrictions.ge("bin", bin));
				
		criteria.add(criterion); 		
		try {
			List<ConfidenceData> objList = (List<ConfidenceData>) criteria.list();
			return objList;
		} catch (HibernateException e) {
			logger.error("exception", e);
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<ConfidenceData> getDataByCrisisInBin(String crisisCode, Integer bin) {
		Criteria criteria = getCurrentSession().createCriteria(ConfidenceData.class);
		Criterion criterion = Restrictions.conjunction()
				.add(Restrictions.eq("crisisCode", crisisCode))
				.add(Restrictions.eq("bin", bin));
		criteria.add(criterion); 
		try {
			List<ConfidenceData> objList = (List<ConfidenceData>) criteria.list();
			return objList;
		} catch (HibernateException e) {
			logger.error("exception", e);
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<ConfidenceData> getDataByCrisisAttributeLabelInBin(String crisisCode, String attributeCode, String labelCode,
																		Integer bin) {
		Criteria criteria = getCurrentSession().createCriteria(ConfidenceData.class);
		Criterion criterion = Restrictions.conjunction()
				.add(Restrictions.eq("crisisCode", crisisCode))
				.add(Restrictions.eq("attributeCode", attributeCode))
				.add(Restrictions.eq("labelCode", labelCode))
				.add(Restrictions.eq("bin", bin));
		criteria.add(criterion); 		
		try {
			List<ConfidenceData> objList = (List<ConfidenceData>) criteria.list();
			return objList;
		} catch (HibernateException e) {
			logger.error("exception", e);
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<ConfidenceData> getDataByCrisisAttributeLabelGranularityInBin(String crisisCode, String attributeCode, String labelCode,
																		Long granularity, Integer bin) {
		Criteria criteria = getCurrentSession().createCriteria(ConfidenceData.class);
		Criterion criterion = Restrictions.conjunction()
				.add(Restrictions.eq("crisisCode", crisisCode))
				.add(Restrictions.eq("attributeCode", attributeCode))
				.add(Restrictions.eq("labelCode", labelCode))
				.add(Restrictions.eq("granularity", granularity))
				.add(Restrictions.eq("bin", bin));
		criteria.add(criterion); 		
		try {
			List<ConfidenceData> objList = (List<ConfidenceData>) criteria.list();
			return objList;
		} catch (HibernateException e) {
			logger.error("exception", e);
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<ConfidenceData> getDataAfterTimestampInBin(String crisisCode, String attributeCode, String labelCode, 
																		Long timestamp, Integer bin) {
		Criteria criteria = getCurrentSession().createCriteria(ConfidenceData.class);
		Criterion criterion = Restrictions.conjunction()
				.add(Restrictions.eq("crisisCode", crisisCode))
				.add(Restrictions.eq("attributeCode", attributeCode))
				.add(Restrictions.eq("labelCode", labelCode))
				.add(Restrictions.ge("timestamp", timestamp))
				.add(Restrictions.eq("bin", bin));
				
		criteria.add(criterion); 		
		try {
			List<ConfidenceData> objList = (List<ConfidenceData>) criteria.list();
			return objList;
		} catch (HibernateException e) {
			logger.error("exception", e);
			e.printStackTrace();
		}
		return null;	
	}

	@Override
	public List<ConfidenceData> getDataAfterTimestampGranularityInBin(String crisisCode, String attributeCode, String labelCode,
																		Long timestamp, Long granularity, Integer bin) {
		Criteria criteria = getCurrentSession().createCriteria(ConfidenceData.class);
		Criterion criterion = Restrictions.conjunction()
				.add(Restrictions.eq("crisisCode", crisisCode))
				.add(Restrictions.eq("attributeCode", attributeCode))
				.add(Restrictions.eq("labelCode", labelCode))
				.add(Restrictions.ge("timestamp", timestamp))
				.add(Restrictions.eq("granularity", granularity))
				.add(Restrictions.eq("bin", bin));
				
		criteria.add(criterion); 		
		try {
			List<ConfidenceData> objList = (List<ConfidenceData>) criteria.list();
			return objList;
		} catch (HibernateException e) {
			logger.error("exception", e);
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<ConfidenceData> getDataBeforeTimestampInBin(String crisisCode, String attributeCode, String labelCode, 
																		Long timestamp, Integer bin) {
		Criteria criteria = getCurrentSession().createCriteria(ConfidenceData.class);
		Criterion criterion = Restrictions.conjunction()
				.add(Restrictions.eq("crisisCode", crisisCode))
				.add(Restrictions.eq("attributeCode", attributeCode))
				.add(Restrictions.eq("labelCode", labelCode))
				.add(Restrictions.le("timestamp", timestamp))
				.add(Restrictions.eq("bin", bin));
				
		criteria.add(criterion); 		
		try {
			List<ConfidenceData> objList = (List<ConfidenceData>) criteria.list();
			return objList;
		} catch (HibernateException e) {
			logger.error("exception", e);
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<ConfidenceData> getDataBeforeTimestampGranularityInBin(String crisisCode, String attributeCode, String labelCode,
																		Long timestamp, Long granularity, Integer bin) {
		Criteria criteria = getCurrentSession().createCriteria(ConfidenceData.class);
		Criterion criterion = Restrictions.conjunction()
				.add(Restrictions.eq("crisisCode", crisisCode))
				.add(Restrictions.eq("attributeCode", attributeCode))
				.add(Restrictions.eq("labelCode", labelCode))
				.add(Restrictions.le("timestamp", timestamp))
				.add(Restrictions.eq("granularity", granularity))
				.add(Restrictions.eq("bin", bin));
				
		criteria.add(criterion); 		
		try {
			List<ConfidenceData> objList = (List<ConfidenceData>) criteria.list();
			return objList;
		} catch (HibernateException e) {
			logger.error("exception", e);
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<ConfidenceData> getDataInIntervalInBin(String crisisCode, String attributeCode, String labelCode, Long timestamp1,
																		Long timestamp2, Integer bin) {
		Criteria criteria = getCurrentSession().createCriteria(ConfidenceData.class);
		Criterion criterion = Restrictions.conjunction()
				.add(Restrictions.eq("crisisCode", crisisCode))
				.add(Restrictions.eq("attributeCode", attributeCode))
				.add(Restrictions.eq("labelCode", labelCode))
				.add(Restrictions.ge("timestamp", timestamp1))
				.add(Restrictions.le("timestamp", timestamp2))
				.add(Restrictions.eq("bin", bin));
				
		criteria.add(criterion); 		
		try {
			List<ConfidenceData> objList = (List<ConfidenceData>) criteria.list();
			return objList;
		} catch (HibernateException e) {
			logger.error("exception", e);
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<ConfidenceData> getDataInIntervalWithGranularityInBin(String crisisCode, String attributeCode, String labelCode,
																		Long timestamp1, Long timestamp2, Long granularity, Integer bin) {
		Criteria criteria = getCurrentSession().createCriteria(ConfidenceData.class);
		Criterion criterion = Restrictions.conjunction()
				.add(Restrictions.eq("crisisCode", crisisCode))
				.add(Restrictions.eq("attributeCode", attributeCode))
				.add(Restrictions.eq("labelCode", labelCode))
				.add(Restrictions.eq("granularity", granularity))
				.add(Restrictions.ge("timestamp", timestamp1))
				.add(Restrictions.le("timestamp", timestamp2))
				.add(Restrictions.eq("bin", bin));
				
		criteria.add(criterion); 		
		try {
			List<ConfidenceData> objList = (List<ConfidenceData>) criteria.list();
			return objList;
		} catch (HibernateException e) {
			logger.error("exception", e);
			e.printStackTrace();
		}
		return null;
	}
}
