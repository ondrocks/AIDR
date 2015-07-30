package qa.qcri.aidr.trainer.api.dao.impl;

import java.io.Serializable;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.springframework.beans.factory.annotation.Autowired;

import qa.qcri.aidr.trainer.api.dao.AbstractDao;

public abstract class AbstractDaoImpl<E, I extends Serializable> implements AbstractDao<E,I> {

	private Class<E> entityClass;

	protected AbstractDaoImpl(Class<E> entityClass) {
		this.entityClass = entityClass;
	}

	private static Logger logger = Logger.getLogger(AbstractDaoImpl.class);

	@Autowired
	private SessionFactory sessionFactory;

	public Session getCurrentSession() {
		return sessionFactory.getCurrentSession();
	}

	@Override
	public E findById(I id) {
		return (E) getCurrentSession().get(entityClass, id);
	}

	@Override
	public void saveOrUpdate(E e) {
		Session session = getCurrentSession();
		session.saveOrUpdate(e);
	}

	@Override
	public void saveOrMerge(E e) {
		Session session = getCurrentSession();
		session.merge(e);
	}

	@Override
	public void save(E e) {
		Session session = getCurrentSession();
		session.save(e);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List findAll() {
		Criteria criteria = getCurrentSession().createCriteria(entityClass);
		criteria.setProjection(Projections.distinct(Projections.property("id")));
		try {
			List result = criteria.list();
			//System.out.println("result = " + result);
			return result;
		} catch (HibernateException e) {
			logger.error("Error in findAll().");
			return null;
		}

	}

	@Override
	public void delete(E e) {
		Session session = getCurrentSession();
		session.buildLockRequest(LockOptions.UPGRADE).lock(e);
		session.delete(e);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<E> findByCriteria(Criterion criterion) {
		Criteria criteria = getCurrentSession().createCriteria(entityClass);
		criteria.add(criterion);
		try {
			return criteria.list();
		} catch (HibernateException e) {
			logger.error("Error in findByCriteria for criteria : " + criteria.toString());
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<E> getMaxOrderByCriteria(Criterion criterion, String orderBy) {
		Criteria criteria = getCurrentSession().createCriteria(entityClass);
		criteria.add(criterion);
		criteria.addOrder(Order.desc(orderBy));
		criteria.setMaxResults(1);
		try {
			return criteria.list();
		} catch (HibernateException e) {
			logger.error("Error in getMaxOrderByCriteria for criteria : " + criteria.toString());
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<E> findByCriteriaByOrder(Criterion criterion, String[] orderBy, Integer count) {
		Criteria criteria = getCurrentSession().createCriteria(entityClass);
		criteria.add(criterion);
		for(int i = 0; i< orderBy.length; i++){
			criteria.addOrder(Order.desc(orderBy[i]));
		}
		if(count != null){
			criteria.setMaxResults(count);
		}
		try {
			return criteria.list();
		} catch (HibernateException e) {
			logger.error("Error in findbyCriteriaByOrder for criteria : " + criteria.toString());
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<E> findByCriteriaWithAliasByOrder(Criterion criterion, String[] orderBy, Integer count, String aliasTable, Criterion aliasCriterion) {
		Criteria criteria = getCurrentSession().createCriteria(entityClass);
		criteria.add(criterion);
		criteria.createAlias(aliasTable, aliasTable, CriteriaSpecification.LEFT_JOIN).add(aliasCriterion);

		for(int i = 0; i< orderBy.length; i++){
			criteria.addOrder(Order.desc(orderBy[i]));
		}
		if(count != null){
			criteria.setMaxResults(count);
		}
		try {
			return criteria.list();
		} catch (HibernateException e) {
			logger.error("Error in findByCriteriaWithAliasByOrder for criteria : " + criteria.toString());
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<E> findByCriteria(Criterion criterion, Integer count) {
		Criteria criteria = getCurrentSession().createCriteria(entityClass);
		criteria.add(criterion);

		if(count != null){
			criteria.setMaxResults(count);
		}
		try {
			return criteria.list();
		} catch (HibernateException e) {
			logger.error("Error in findByCriteria for criteria : " + criteria.toString());
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public E findByCriterionID(Criterion criterion) {
		Criteria criteria = getCurrentSession().createCriteria(entityClass);
		criteria.add(criterion);
		try {
			return (E) criteria.uniqueResult();
		} catch (HibernateException e) {
			logger.error("Error in findByCriterionID for criteria : " + criteria.toString());
			return null;
		}
	}
}
