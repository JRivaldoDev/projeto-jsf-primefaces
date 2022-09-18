package bean;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;

import dao.DaoGeneric;
import model.Cidades;
import model.Estados;
import net.bootsfaces.component.selectOneMenu.SelectOneMenu;

public class EstadosCidadesBean{
	
	private List<SelectItem> estados = new ArrayList<SelectItem>();
	private List<SelectItem> cidades = new ArrayList<SelectItem>();
	private Estados estado = new Estados();
	private Cidades cidade = new Cidades();
	
	public List<SelectItem> getEstados() {
		listarEstados();
		return estados;
	}
	public void setEstados(List<SelectItem> estados) {
		this.estados = estados;
	}
	public List<SelectItem> getCidades() {
		return cidades;
	}
	public void setCidades(List<SelectItem> cidades) {
		this.cidades = cidades;
	}
	public Estados getEstado() {
		return estado;
	}
	public void setEstado(Estados estado) {
		this.estado = estado;
	}
	public Cidades getCidade() {
		return cidade;
	}
	public void setCidade(Cidades cidade) {
		this.cidade = cidade;
	}
	
	private void listarEstados(){
		
		DaoGeneric<Estados> daoEstados = new DaoGeneric<Estados>();
		
		List<Estados> listEstados = daoEstados.listar(Estados.class);
		
		for (Estados est : listEstados) {
			estados.add(new SelectItem(est, est.getNome()));
		}
		
		
	}
	
			
	@SuppressWarnings("unchecked")
	public void carregarCidades(AjaxBehaviorEvent event) {
		DaoGeneric<Cidades> daoCidades = new DaoGeneric<Cidades>();
		
		estado = (Estados) ((SelectOneMenu) event.getSource()).getValue();
		
		if(estado != null) {
			List<Cidades> listCidades = daoCidades.getEntityManager().createQuery("from Cidades where estados= :estados")
					.setParameter("estados", estado).getResultList();
			
			cidades = new ArrayList<SelectItem>();
			for (Cidades cid : listCidades) {
				cidades.add(new SelectItem(cid, cid.getNome()));
			}
		}

	}
	
	@SuppressWarnings("unchecked")
	public void carregarCidadesPorEstado(Estados estado) {
		DaoGeneric<Cidades> daoCidades = new DaoGeneric<Cidades>();
				
		if(estado != null) {
			
			List<Cidades> listCidades = daoCidades.getEntityManager().createQuery("from Cidades where estados= :estados")
					.setParameter("estados", estado).getResultList();
			
			cidades = new ArrayList<SelectItem>();
			for (Cidades cid : listCidades) {
				cidades.add(new SelectItem(cid, cid.getNome()));
			}
		}

	}
	
	public Estados buscarEstado(String id) {
		DaoGeneric<Estados> daoEstados = new DaoGeneric<Estados>();
		Estados uf = daoEstados.buscar(Long.parseLong(id), Estados.class);
				
		return uf;
	}
	
}
