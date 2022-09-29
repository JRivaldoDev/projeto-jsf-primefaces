package filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import model.Pessoa;

@WebFilter(urlPatterns = {"/principal/*"})
public class FilterLogin implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		HttpServletRequest request2 = (HttpServletRequest) request;
		HttpSession session = request2.getSession();
		
		Pessoa usuarioLogado = (Pessoa) session.getAttribute("usuarioLogado");
		String url = request2.getServletPath();
		
		if(!url.equalsIgnoreCase("index.jsf") && usuarioLogado == null) {
			RequestDispatcher dispatcher = request.getRequestDispatcher("/index.jsf");
			dispatcher.forward(request2, response);
			return;
		}
		else {
			chain.doFilter(request, response);
		}
		
	}

}
