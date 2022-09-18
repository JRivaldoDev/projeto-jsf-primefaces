package util;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class HibernateUtil {
	
private static EntityManagerFactory factory = null;
	
	static  {
		init();
	}
	
	private static void init() {
		
		try {
			
			if(factory == null) {
				factory = Persistence.createEntityManagerFactory("projeto-jsf-primefaces");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static EntityManager getEntityManager() {
		return factory.createEntityManager(); //provê a persistencia
	}
	
	public static Object getPrimaryKey(Object entidade) {
		return factory.getPersistenceUnitUtil().getIdentifier(entidade);
	}

}
