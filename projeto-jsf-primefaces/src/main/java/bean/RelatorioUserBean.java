package bean;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import dao.DaoGeneric;
import model.Pessoa;

@ViewScoped
@ManagedBean (name = "relatorioUserBean")
public class RelatorioUserBean implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String nome;
	private String estado;
	private String nivelProgramador;
	private Date dataNascimento;
	private List<Pessoa> pessoas = new ArrayList<Pessoa>();
	private DaoGeneric<Pessoa> daoPessoa = new DaoGeneric<Pessoa>();
	private EstadosCidadesBean estadosCidadesBean = new EstadosCidadesBean();
	
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public String getEstado() {
		return estado;
	}
	public void setEstado(String estado) {
		this.estado = estado;
	}
	public String getNivelProgramador() {
		return nivelProgramador;
	}
	public void setNivelProgramador(String nivelProgramador) {
		this.nivelProgramador = nivelProgramador;
	}
	public Date getDataNascimento() {
		return dataNascimento;
	}
	public void setDataNascimento(Date dataNascimento) {
		this.dataNascimento = dataNascimento;
	}
	public List<Pessoa> getPessoas() {
		return pessoas;
	}
	public void setPessoas(List<Pessoa> pessoas) {
		this.pessoas = pessoas;
	}
	public EstadosCidadesBean getEstadosCidadesBean() {
		return estadosCidadesBean;
	}
	public void setEstadosCidadesBean(EstadosCidadesBean estadosCidadesBean) {
		this.estadosCidadesBean = estadosCidadesBean;
	}

	@SuppressWarnings("unchecked")
	public void buscar() {
				
		try {
			
			boolean contem = false;
			StringBuilder sql = new StringBuilder();
			sql.append("select p from Pessoa p ");
			
			if(!nome.isEmpty() || !estado.isEmpty() || !nivelProgramador.isEmpty() || dataNascimento != null) {
				sql.append("where ");
			}
			
			if(!nome.isEmpty()) {
				sql.append("upper(p.nome) like '%" + nome.toUpperCase() +"%' ");
				contem = true;
			}
			
			if(!nivelProgramador.isEmpty() && !contem) {
				sql.append("p.nivelProgramador= '" + nivelProgramador +"' ");
				contem = true;
			}
			else if(!nivelProgramador.isEmpty()){
				sql.append("and p.nivelProgramador= '" + nivelProgramador +"' ");
			}
			
			if(dataNascimento != null && !contem) {
				sql.append("p.dataNascimento >= '" + new SimpleDateFormat("yyyy-MM-dd").format(dataNascimento) +"' ");
				contem = true;
			}
			else if(dataNascimento != null) {
				sql.append("and p.dataNascimento >= '" + new SimpleDateFormat("yyyy-MM-dd").format(dataNascimento) +"' ");
			}
			
			if(!estado.isEmpty()  && !contem) {
				sql.append("p.estados= :estados ");
				pessoas = daoPessoa.getEntityManager().createQuery(sql.toString() + " order by p.nome asc")
						.setParameter("estados", estadosCidadesBean.buscarEstado(estado))
						.getResultList();
			}
			else if(!estado.isEmpty()){
				sql.append("and p.estados= :estados ");
				pessoas = daoPessoa.getEntityManager().createQuery(sql.toString() + " order by p.nome asc")
						.setParameter("estados", estadosCidadesBean.buscarEstado(estado))
						.getResultList();
			}
			else {
				pessoas = daoPessoa.getEntityManager().createQuery(sql.toString() + " order by p.nome asc").getResultList();
			}
			
									
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
}
