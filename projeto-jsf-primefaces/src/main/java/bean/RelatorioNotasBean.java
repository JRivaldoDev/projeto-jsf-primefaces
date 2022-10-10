package bean;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import dao.DaoGeneric;
import model.Lancamento;

@ViewScoped
@ManagedBean (name = "relatorioNotasBean")
public class RelatorioNotasBean implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String usuario;
	private String empresaOrigem;
	private String empresaDestino;
	private Date dataEnvio;
	private List<Lancamento> lancamentos = new ArrayList<Lancamento>();
	private DaoGeneric<Lancamento> daoLancamento = new DaoGeneric<Lancamento>();
	
	public List<Lancamento> getLancamentos() {
		return lancamentos;
	}
	public void setLancamentos(List<Lancamento> lancamentos) {
		this.lancamentos = lancamentos;
	}
	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}
	public String getUsuario() {
		return usuario;
	}
	public void setEmpresaOrigem(String empresaOrigem) {
		this.empresaOrigem = empresaOrigem;
	}
	public String getEmpresaOrigem() {
		return empresaOrigem;
	}
	public void setEmpresaDestino(String empresaDestino) {
		this.empresaDestino = empresaDestino;
	}
	public String getEmpresaDestino() {
		return empresaDestino;
	}
	public void setDataEnvio(Date dataEnvio) {
		this.dataEnvio = dataEnvio;
	}
	public Date getDataEnvio() {
		return dataEnvio;
	}
	
	@SuppressWarnings("unchecked")
	public String buscar() {
		
		try {
			
			boolean contem = false;
			StringBuilder sql = new StringBuilder();
			sql.append("select l from Lancamento l ");
			
			if(!usuario.isEmpty() || !empresaOrigem.isEmpty() || !empresaDestino.isEmpty() || dataEnvio != null) {
				sql.append("where ");
			}
			
			if(!usuario.isEmpty()) {
				sql.append("upper(l.usuario.nome) like '%" + usuario.toUpperCase() +"%' ");
				contem = true;
			}
			
			if(!empresaOrigem.isEmpty() && !contem) {
				sql.append("upper(l.empresaOrigem) like '%" + empresaOrigem.toUpperCase() +"%' ");
				contem = true;
			}
			else if(!empresaOrigem.isEmpty()){
				sql.append("and upper(l.empresaOrigem) like '%" + empresaOrigem.toUpperCase() +"%' ");
			}
			
			if(!empresaDestino.isEmpty() && !contem) {
				sql.append("upper(l.empresaDestino) like '%" + empresaDestino.toUpperCase() +"%' ");
				contem = true;
			}
			else if(!empresaDestino.isEmpty()){
				sql.append("and upper(l.empresaDestino) like '%" + empresaDestino.toUpperCase() +"%' ");
			}
			
			if(dataEnvio != null && !contem) {
				sql.append("l.dataEnvio= '" + new SimpleDateFormat("yyyy-MM-dd").format(dataEnvio) +"' ");
			}
			else if(dataEnvio != null) {
				sql.append("and l.dataEnvio= '" + new SimpleDateFormat("yyyy-MM-dd").format(dataEnvio) +"' ");
			}
			
			lancamentos = daoLancamento.getEntityManager().createQuery(sql.toString() + " order by l.id desc").getResultList();
			
			usuario = "";
			empresaOrigem = "";
			empresaDestino = "";
			dataEnvio = null;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return "";
	}

}
