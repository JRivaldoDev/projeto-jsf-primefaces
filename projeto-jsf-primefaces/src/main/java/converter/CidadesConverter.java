package converter;

import java.io.Serializable;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import dao.DaoGeneric;
import model.Cidades;

@FacesConverter(value = "cidadesConverter", forClass = Cidades.class)
public class CidadesConverter implements Converter, Serializable {

	
	private static final long serialVersionUID = 1L;

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		
		DaoGeneric<Cidades> daoCidades = new DaoGeneric<Cidades>();
		
		Cidades cidade = new Cidades();
		
		if(value != null && !value.toString().endsWith("-")) {
			cidade = daoCidades.buscar(Long.parseLong(value), Cidades.class);
		}
		
		return cidade;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		
		if(value != null && (value instanceof Cidades)) {
			
			return String.valueOf(((Cidades) value).getId());
		}
		
		return null;
		
	}
	

}
