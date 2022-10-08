package bean;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
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

import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.line.LineChartDataSet;
import org.primefaces.model.charts.line.LineChartModel;
import org.primefaces.model.charts.line.LineChartOptions;
import org.primefaces.model.charts.optionconfig.title.Title;

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
	private LineChartModel lineChartModel = new LineChartModel();
	
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
	
	public LineChartModel getLineChartModel() {
		return lineChartModel;
	}
	public void setLineChartModel(LineChartModel lineChartModel) {
		this.lineChartModel = lineChartModel;
	}

	public String salvar() {
		if(existeLogin(pessoa.getLogin())) {
			mostrarMsg(FacesMessage.SEVERITY_WARN, "Já existe um usuário com o login informado!");
		}else {
			if(pessoa.getId() == null) {
				daoPessoa.salvar(pessoa);
			}
			else {
				daoPessoa.salvarMerge(pessoa);
			}
			mostrarMsg(FacesMessage.SEVERITY_INFO, "Operação realizada com sucesso!!!");
			pessoa = new Pessoa();
			carregarPessoas();
		}
		
		return "";
	}
	
	private boolean existeLogin(String login) {
		boolean existe = false;
		
		try {
		Pessoa	usuario = (Pessoa) daoPessoa.getEntityManager()
					.createQuery("from Pessoa where upper(login)= :login" )
					.setParameter("login", login.toUpperCase()).getSingleResult();
		
		if(usuario != null) {
			existe = true;
			
			if(pessoa.getId() == usuario.getId()) {
				existe = false;
			}
		}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
				
		return existe;
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

	public void deletar() {
		daoPessoa.deletar(pessoa);
		novo();
		mostrarMsg(FacesMessage.SEVERITY_INFO, "Cadastro deletado com sucesso!!!");
	}

	@PostConstruct
	public void carregarPessoas() {
		pessoas = daoPessoa.listar(Pessoa.class);
	}

	public String logar() {
		
		try {
			
		Pessoa usuarioLogado = (Pessoa) daoPessoa.getEntityManager()
				.createQuery("from Pessoa where login= :login and senha = :senha")
				.setParameter("login", pessoa.getLogin()).setParameter("senha", pessoa.getSenha()).getSingleResult();

		if (usuarioLogado != null) {
			FacesContext context = FacesContext.getCurrentInstance();
			ExternalContext externalContext = context.getExternalContext();

			HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
			HttpSession session = request.getSession();

			session.setAttribute("usuarioLogado", usuarioLogado);
			return "/principal/primeira_pagina.jsf";

		}
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage("msg", new FacesMessage(FacesMessage.SEVERITY_WARN,"","Usuário não encontrado!"));
		}

		return "/index.jsf";
	}

	public String deslogar() {
		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext externalContext = context.getExternalContext();

		HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
		request.getSession().invalidate();

		return "/index.jsf";
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


		} catch (Exception e) {
			e.printStackTrace();
			mostrarMsg(FacesMessage.SEVERITY_ERROR, "Erro ao pesquisar CEP!");
		}
	}

	private void mostrarMsg(Severity tipo, String msg ) {
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(tipo, "", msg));
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
	
	public void gerarGraficoSalario() {
		carregarPessoas();
		
		ChartData data = new ChartData();
        LineChartDataSet dataSet = new LineChartDataSet();
        
        List<Object> values = new ArrayList<>();
        List<String> labels = new ArrayList<>();
		
		Double salJunior = 0.0;
		int qntJ = 0;
		Double salPleno = 0.0;
		int qntP = 0;
		Double salSenior = 0.0;
		int qntS = 0;
		Double salEspecialista = 0.0;
		int qntE = 0;

		for (Pessoa pessoa : pessoas) {
			
			if(pessoa.getNivelProgramador().equalsIgnoreCase("JUNIOR")) {
				salJunior += pessoa.getSalario();
				qntJ++;
			}
			else if (pessoa.getNivelProgramador().equalsIgnoreCase("PLENO")) {
				salPleno += pessoa.getSalario();
				qntP++;
			}
			else if (pessoa.getNivelProgramador().equalsIgnoreCase("SENIOR")) {
				salSenior += pessoa.getSalario();
				qntS++;
			}
			else if (pessoa.getNivelProgramador().equalsIgnoreCase("ESPECIALISTA")) {
				salEspecialista += pessoa.getSalario();
				qntE++;
			}
		}
		
		if(qntJ > 0) {
			labels.add("Junior");
			values.add(salJunior / qntJ);
		}
		if(qntP > 0) {
			labels.add("Pleno");
			values.add(salPleno / qntP);
		}
		if(qntS > 0) {
			labels.add("Senior");
			values.add(salSenior / qntS);
		}
		if(qntE > 0) {
			labels.add("Especialista");
			values.add(salEspecialista / qntE);
		}
		
		dataSet.setData(values);
		dataSet.setLabel("Média");
		dataSet.setFill(false);
		dataSet.setBorderColor("rgb(75, 192, 192)");
        dataSet.setTension(0.1);
		data.addChartDataSet(dataSet);
		data.setLabels(labels);
		
		LineChartOptions options = new LineChartOptions();
		Title title = new Title();
        title.setDisplay(true);
        title.setText("Média de Salários");
        options.setTitle(title);
		
		lineChartModel.setData(data);
		lineChartModel.setOptions(options);
	}
	
}
