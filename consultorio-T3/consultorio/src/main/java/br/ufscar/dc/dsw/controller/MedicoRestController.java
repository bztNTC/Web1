package br.ufscar.dc.dsw.controller;

import java.io.IOException;
import java.util.List;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import br.ufscar.dc.dsw.domain.Medico;
import br.ufscar.dc.dsw.service.spec.IMedicoService;

@RestController
public class MedicoRestController {
    
    @Autowired
    private IMedicoService service;

    @Autowired
    BCryptPasswordEncoder encoder;

    private boolean isJSONValid(String jsonInString) {
		try {
			return new ObjectMapper().readTree(jsonInString) != null;
		} catch (IOException e) {
			return false;
		}
	}

	private void parse(Medico medico, JSONObject json) {

		Object id = json.get("id");
		if (id != null) {
			if (id instanceof Integer) {
				medico.setId(((Integer) id).longValue());
			} else {
				medico.setId((Long) id);
			}
		}

        medico.setNome((String) json.get("nome"));
        medico.setPassword(encoder.encode((String) json.get("password")));
        medico.setEmail((String) json.get("email"));
        medico.setCRM((String) json.get("crm"));
        medico.setEspecialidade((String) json.get("especialidade"));
	}

    @GetMapping(path = "/medicos")
	public ResponseEntity<List<Medico>> lista() {
		List<Medico> lista = service.buscarTodos();
		if (lista.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(lista);
 	}

     @DeleteMapping(path = "medicos/{id}")
     public ResponseEntity<Boolean> remove(@PathVariable("id") long id) {
         Medico medico = service.buscarPorId(id);
         if (medico == null) {
             return ResponseEntity.notFound().build();
         } else {
             if (service.medicoTemConsultas(id)) {
                 return new ResponseEntity<Boolean>(false, HttpStatus.FORBIDDEN);
             } else {
                 service.excluir(id);
                 return new ResponseEntity<Boolean>(true, HttpStatus.OK);
             }
         }
     }
	
    
    @GetMapping(path = "/medicos/{id}")
	public ResponseEntity<Medico> lista(@PathVariable("id") long id) {
        Medico medico = service.buscarPorId(id);
		if (medico == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(medico);
	}

    @GetMapping(path = "/medicos/especialidades/{nome}")
	public ResponseEntity<List<Medico>> lista(@PathVariable("nome") String especialidade) {
        List<Medico> lista = service.buscarPorEspecialidade(especialidade);
		if (lista.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(lista);
	}

    @PostMapping(path = "/medicos")
	@ResponseBody
	public ResponseEntity<Medico> cria(@RequestBody JSONObject json) {
		try {
			if (isJSONValid(json.toString())) {
				Medico medico = new Medico();
				parse(medico, json);
				service.salvar(medico);
				return ResponseEntity.ok(medico);
			} else {
				return ResponseEntity.badRequest().body(null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(null);
		}
 	}

    
    @PutMapping(path = "medicos/{id}")
    public ResponseEntity<Medico> atualiza(@PathVariable("id") long id, @RequestBody JSONObject json) {
        try {
            if (isJSONValid(json.toString())) {
                Medico medico = service.buscarPorId(id);
                if (medico == null) {
                    return ResponseEntity.notFound().build();
                } else {
                    parse(medico, json);
                    service.salvar(medico);
                    return ResponseEntity.ok(medico);
                }
            } else {
                return ResponseEntity.badRequest().body(null);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(null);
        }
    }
}
