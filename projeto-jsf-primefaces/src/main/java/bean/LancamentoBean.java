package bean;

import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import dao.DaoGeneric;
import model.Lancamento;
import model.Pessoa;

@ViewScoped
@ManagedBean (name = "lancamentoBean")
public class LancamentoBean {
	
	private Lancamento lancamento = new Lancamento();
	private DaoGeneric<Lancamento> daoLancamento = new DaoGeneric<Lancamento>();
	private List<Lancamento> lancamentos = new ArrayList<Lancamento>();
	
	public List<Lancamento> getLancamentos() {
		carregarLancamentos();
		return lancamentos;
	}
	public void setLancamentos(List<Lancamento> lancamentos) {
		this.lancamentos = lancamentos;
	}
	public Lancamento getLancamento() {
		return lancamento;
	}
	public void setLancamento(Lancamento lancamento) {
		this.lancamento = lancamento;
	}
	
	public String salvar() {
		lancamento.setUsuario(usuarioLogado());
		daoLancamento.salvarMerge(lancamento);
		lancamento = new Lancamento();
		mostrarMsg(FacesMessage.SEVERITY_INFO,"Operação realizada com sucesso!");
		carregarLancamentos();
		return "";
	}
	
	public void remover() {
		daoLancamento.deletar(lancamento);
		lancamento = new Lancamento();
		mostrarMsg(FacesMessage.SEVERITY_INFO,"Operação realizada com sucesso!");
		carregarLancamentos();
	}
	
	public void novo() {
		lancamento = new Lancamento();
	}
	
	public void limpar() {
		Lancamento novo = new Lancamento();
		novo.setId(lancamento.getId());
		novo.setUsuario(lancamento.getUsuario());
		lancamento = novo;
	}
	
	@SuppressWarnings("unchecked")
	public void carregarLancamentos() {
		
		lancamentos = daoLancamento.getEntityManager().createQuery
				("from Lancamento where usuario= :usuario order by id desc")
				.setParameter("usuario", usuarioLogado())
				.getResultList();
		if(lancamentos == null) {
			lancamentos = new ArrayList<Lancamento>();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void carregarLancamentosLimit() {
		
		lancamentos = daoLancamento.getEntityManager().createQuery
				("from Lancamento where usuario= :usuario order by id desc")
				.setParameter("usuario", usuarioLogado())
				.setMaxResults(10)
				.getResultList();
		if(lancamentos == null) {
			lancamentos = new ArrayList<Lancamento>();
		}
	}
	
	private Pessoa usuarioLogado() {
		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext externalContext = context.getExternalContext();
		
		HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
		HttpSession session = request.getSession();
		
		Pessoa usuarioLogado = (Pessoa) session.getAttribute("usuarioLogado");
		
		return usuarioLogado;
	}
	
	private void mostrarMsg(Severity tipo,String msg) {
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(tipo, "", msg));
	}
	

}
