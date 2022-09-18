package dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import util.HibernateUtil;

public class DaoGeneric<E> {
	
private EntityManager entityManager = HibernateUtil.getEntityManager();
	
	public void salvar(E entidade) {
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();
		entityManager.persist(entidade);
		transaction.commit();
	}
	
	public E salvarMerge(E entidade) {
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();
		E retorno = entityManager.merge(entidade);
		transaction.commit();
		return retorno;
	}
	
	public E atualizar(E entidade) {
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();
		E atualizado = entityManager.merge(entidade);
		transaction.commit();
		return atualizado;
	}
	
	public E buscar(Long id, Class<E> entidade) {
				
		return entityManager.find(entidade, id);
	}
	
	public void deletar(E entidade) {
		Object id = HibernateUtil.getPrimaryKey(entidade);
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();
		entityManager.createNativeQuery("delete from " + 
		entidade.getClass().getSimpleName().toLowerCase() + 
		" where id=" + id).executeUpdate();
		transaction.commit();
		
	}
	
	public List<E> listar(Class<E> entidade){
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();
		
		@SuppressWarnings("unchecked")
		List<E> lista = entityManager.createQuery("from " + entidade.getName() + " order by id").getResultList();
		transaction.commit();
		
		return lista;
	}
	
	public List<E> listarLimitado(Class<E> entidade){
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();
		
		@SuppressWarnings("unchecked")
		List<E> lista = entityManager.createQuery("from " + entidade.getName() + " order by id desc")
		.setMaxResults(10).getResultList();
		transaction.commit();
		
		return lista;
	}
	
	public EntityManager getEntityManager() {
		return entityManager;
	}

}
