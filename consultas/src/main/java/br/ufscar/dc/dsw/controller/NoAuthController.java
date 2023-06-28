package br.ufscar.dc.dsw.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.ufscar.dc.dsw.dao.MedicoDAO;
import br.ufscar.dc.dsw.domain.Usuario;

@WebServlet(urlPatterns = "/noAuth/*")
public class NoAuthController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private MedicoDAO dao;

    @Override
    public void init() {
        dao = new MedicoDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getPathInfo();
        if (action == null) {
            action = "/";
        }

        switch (action) {
            case "/listaMedicosEspecialidade":
                listaMedicosEspecialidade(request, response);
                break;
            default:
                listaMedicos(request, response);
                break;
        }
    }

    private void listaMedicos(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Usuario> listaMedicos = dao.getAll();
        request.setAttribute("listaMedicos", listaMedicos);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/noAuthView/consulta-medicos.jsp");
        dispatcher.forward(request, response);
    }

    private void listaMedicosEspecialidade(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Obtenha a especialidade selecionada a partir do parâmetro da requisição
        String especialidade = request.getParameter("especialidade");

        // Consulte o banco de dados para obter a lista de médicos por especialidade
        List<Usuario> listaMedicosEspecialidade = dao.getByEspecialidade(especialidade);

        // Armazene a lista de médicos por especialidade no atributo de requisição
        request.setAttribute("listaMedicosEspecialidade", listaMedicosEspecialidade);

        // Encaminhe a requisição para a página "listaMedicosEspecialidade.jsp"
        RequestDispatcher dispatcher = request.getRequestDispatcher("/noAuthView/listaMedicosEspecialidade.jsp");
        dispatcher.forward(request, response);
    }
}
