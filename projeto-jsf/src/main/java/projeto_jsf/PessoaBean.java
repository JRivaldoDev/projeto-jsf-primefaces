package projeto_jsf;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import javax.xml.bind.DatatypeConverter;

import com.google.gson.Gson;

import dao.DaoGeneric;
import model.Pessoa;
import util.EnderecoJsonUtil;

@ViewScoped
@ManagedBean(name = "pessoaBean")
public class PessoaBean {

	private Pessoa pessoa = new Pessoa();
	private DaoGeneric<Pessoa> daoPessoa = new DaoGeneric<Pessoa>();
	private List<Pessoa> pessoas = new ArrayList<Pessoa>();
	private EstadosCidadesBean estadosCidadesBean = new EstadosCidadesBean();
	private Part arquivoFoto;

	public Pessoa getPessoa() {
		return pessoa;
	}

	public void setPessoa(Pessoa pessoa) {
		this.pessoa = pessoa;
	}

	public List<Pessoa> getPessoas() {
		return pessoas;
	}

	public EstadosCidadesBean getEstadosCidadesBean() {
		return estadosCidadesBean;
	}

	public void setEstadosCidadesBean(EstadosCidadesBean estadosCidadesBean) {
		this.estadosCidadesBean = estadosCidadesBean;
	}

	public Part getArquivoFoto() {
		return arquivoFoto;
	}

	public void setArquivoFoto(Part arquivoFoto) {
		this.arquivoFoto = arquivoFoto;
	}

	public void salvar() {
		pessoa = daoPessoa.salvarMerge(pessoa);
		mostrarMsg("Operação realizada com sucesso!!!");
		carregarPessoas();
	}

	public void novo() {
		pessoa = new Pessoa();
		carregarPessoas();
	}

	public void limpar() {
		Pessoa nova = new Pessoa();
		nova.setId(pessoa.getId());
		pessoa = nova;
	}

	public String deletar() {
		daoPessoa.deletar(pessoa);
		pessoa = new Pessoa();
		mostrarMsg("Cadastro deletado com sucesso!!!");
		carregarPessoas();
		return "";
	}

	@PostConstruct
	public void carregarPessoas() {
		pessoas = daoPessoa.listar(Pessoa.class);
	}

	public String logar() {

		Pessoa usuarioLogado = (Pessoa) daoPessoa.getEntityManager()
				.createQuery("from Pessoa where login= :login and senha = :senha")
				.setParameter("login", pessoa.getLogin()).setParameter("senha", pessoa.getSenha()).getSingleResult();

		if (usuarioLogado != null) {
			FacesContext context = FacesContext.getCurrentInstance();
			ExternalContext externalContext = context.getExternalContext();

			HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
			HttpSession session = request.getSession();

			session.setAttribute("usuarioLogado", usuarioLogado);
			return "primeira_pagina.jsf";

		}

		return "index.jsf";
	}

	public String deslogar() {

		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext externalContext = context.getExternalContext();

		HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
		request.getSession().invalidate();

		return "index.jsf";
	}

	public boolean permitirAcesso(String perfil) {

		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext externalContext = context.getExternalContext();

		HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
		HttpSession session = request.getSession();

		Pessoa usuarioLogado = (Pessoa) session.getAttribute("usuarioLogado");

		return usuarioLogado.getPerfil().equalsIgnoreCase(perfil);
	}

	public void pesquisarCep(AjaxBehaviorEvent event) {

		try {
			URL url = new URL("https://viacep.com.br/ws/" + pessoa.getCep() + "/json/");
			URLConnection connection = url.openConnection();
			InputStream stream = connection.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));

			String cep = "";
			StringBuilder jsonCep = new StringBuilder();

			while ((cep = reader.readLine()) != null) {
				jsonCep.append(cep);
			}

			EnderecoJsonUtil endereco = new Gson().fromJson(jsonCep.toString(), EnderecoJsonUtil.class);

			pessoa.setCep(endereco.getCep());
			pessoa.setRua(endereco.getLogradouro());
			pessoa.setBairro(endereco.getBairro());
			pessoa.setComplemento(endereco.getComplemento());
			pessoa.setCidade(endereco.getLocalidade());
			pessoa.setUf(endereco.getUf());

			System.out.println(endereco);

		} catch (Exception e) {
			e.printStackTrace();
			mostrarMsg("Erro ao pesquisar CEP!");
		}
	}

	private void mostrarMsg(String msg) {
		FacesContext context = FacesContext.getCurrentInstance();
		FacesMessage message = new FacesMessage(msg);
		context.addMessage(null, message);
	}

	public void editar() {

		if (pessoa.getCidades() != null) {
			estadosCidadesBean.carregarCidadesPorEstado(pessoa.getEstados());
		}
	}

	private byte[] getByte(InputStream stream) throws Exception {

		int largura;
		int tamanho = 1024;
		byte[] buff = null;

		if (stream instanceof ByteArrayInputStream) {
			tamanho = stream.available();
			buff = new byte[tamanho];
			largura = stream.read(buff, 0, tamanho);
		} else {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			buff = new byte[tamanho];
			while ((largura = stream.read(buff, 0, tamanho)) != -1) {
				outputStream.write(buff, 0, largura);
			}
			buff = outputStream.toByteArray();
		}

		return buff;
	}

	public void processarImagem() {
		
		try {
			//processar imagem
			byte[] imagemByte = getByte(arquivoFoto.getInputStream());
			pessoa.setFotoOriginalBase64(imagemByte); //salva a imagem original
			
			//bufferedImage
			BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imagemByte));
			
			//pegar o tipo da imagem
			int type = bufferedImage.getType() == 0? BufferedImage.TYPE_INT_ARGB : bufferedImage.getType();
			
			//criar a imagem em miniatura
			int width = 200;
			int height = 200;
			BufferedImage resizedImage = new BufferedImage(width, height, type);
			Graphics2D graphics2d = resizedImage.createGraphics();
			graphics2d.drawImage(bufferedImage, 0, 0, width, height, null);
			graphics2d.dispose();
			
			//Escrever novamente a imagem em tamanho menor
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			String extensao = arquivoFoto.getContentType().split("\\/")[1];
			ImageIO.write(resizedImage, extensao, outputStream);
			
			String miniImage = "data:" + arquivoFoto.getContentType() + ";base64," 
					+ DatatypeConverter.printBase64Binary(outputStream.toByteArray());
			
			//processar imagem
			pessoa.setFotoIconBase64(miniImage);
			pessoa.setExtensao(extensao);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void downloadFoto() {
		try {
			
			HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance()
					.getExternalContext().getResponse();
			response.addHeader("Content-Disposition", "attachment; filename=FotoPerfil." + pessoa.getExtensao());
			response.setContentType("application/octed-stream");
			response.setContentLength(pessoa.getFotoOriginalBase64().length);
			response.getOutputStream().write(pessoa.getFotoOriginalBase64());
			response.getOutputStream().flush();
			FacesContext.getCurrentInstance().responseComplete();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
