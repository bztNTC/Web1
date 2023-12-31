package br.ufscar.dc.dsw.controller;

import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.ufscar.dc.dsw.domain.Medico;
import br.ufscar.dc.dsw.service.spec.IMedicoService;
import br.ufscar.dc.dsw.service.spec.IEspecialidadeService;

@Controller
@RequestMapping("/medicos")
public class MedicoController {
    @Autowired
    private IMedicoService service;

    @Autowired
    private IEspecialidadeService especialidadeService;

    @Autowired
	private BCryptPasswordEncoder encoder;

    @GetMapping("/cadastrar")
    public String cadastrar(Medico medico, ModelMap model) {
        medico.setRole("ROLE_MEDICO");
        model.addAttribute("especialidades", especialidadeService.buscarTodos());
        return "medicos/cadastro";
    }

    @GetMapping("/listar")
	public String listar(ModelMap model) {
		model.addAttribute("medicos",service.buscarTodos());
		return "medicos/lista";
	}

    @PostMapping("/salvar")
	public String salvar(@Valid Medico medico, BindingResult result, RedirectAttributes attr, ModelMap model) {

		if (result.hasErrors()) {
            model.addAttribute("especialidades", especialidadeService.buscarTodos());
			return "medicos/cadastro";
		}

		medico.setPassword(encoder.encode(medico.getPassword()));
		service.salvar(medico);

		attr.addFlashAttribute("success", "medico.create.success");
		return "redirect:/medicos/listar";
	}

    @GetMapping("/editar/{id}")
    public String preEditar(@PathVariable("id") Long id, ModelMap model) {
        model.addAttribute("medico", service.buscarPorId(id));
        model.addAttribute("especialidades", especialidadeService.buscarTodos());
        return "medicos/cadastro";
    }

    @PostMapping("/editar")
    public String editar(@Valid Medico medico, String novoPassword, BindingResult result, ModelMap model, RedirectAttributes attr) {

        if (result.hasErrors()) {
            model.addAttribute("especialidades", especialidadeService.buscarTodos());
            return "medicos/cadastro";
        }

        // if (result.getFieldErrorCount() > 1 || result.getFieldError("CRM") == null) {
        //     model.addAttribute("especialidades", especialidadeService.buscarTodos());
        //     return "medicos/cadastro";
        // }

        if (novoPassword != null && !novoPassword.trim().isEmpty()) {
            medico.setPassword(encoder.encode(novoPassword));
        } else {
			System.out.println("Senha não foi editada");
		}
        service.salvar(medico);
        attr.addFlashAttribute("success", "medico.edit.success");
        return "redirect:/medicos/listar";
    }

    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable("id") Long id, ModelMap model) {
        if (service.medicoTemConsultas(id)) {
            model.addAttribute("fail", "medico.delete.fail");
        } else {
            service.excluir(id);
            model.addAttribute("success", "medico.delete.success");
        }
        
        return listar(model);
    }
}
