package converter;

import java.io.Serializable;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import dao.DaoGeneric;
import model.Estados;

@FacesConverter(value = "estadosConverter", forClass = Estados.class)
public class EstadosConverter implements Converter, Serializable {

	
	private static final long serialVersionUID = 1L;

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		
		DaoGeneric<Estados> daoEstados = new DaoGeneric<Estados>();
		
		Estados estado = new Estados();
		
		if(value != null) {
			estado = daoEstados.buscar(Long.parseLong(value), Estados.class);
		}
		
		return estado;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		
		if(value != null && (value instanceof Estados)) {
			
			return String.valueOf(((Estados) value).getId());
		}
		
		return null;
		
	}
	

}
