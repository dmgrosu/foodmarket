package md.ramaiana.foodmarket.controller;

import lombok.extern.slf4j.Slf4j;
import md.ramaiana.foodmarket.model.Client;
import md.ramaiana.foodmarket.service.ClientNotFoundException;
import md.ramaiana.foodmarket.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Dmitri Grosu (dmitri.grosu@gmail.com), 2/7/21
 */
@RestController
@RequestMapping("/client")
@Slf4j
public class ClientController {

    private final ClientService clientService;

    @Autowired
    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping("/findByIdno")
    public ResponseEntity<?> getIdByIdno(@RequestParam("idno") String idno) {
        try {
            Client client = clientService.findClientByIdno(idno);
            return ResponseEntity.ok(client.getId());
        } catch (ClientNotFoundException e) {
            log.warn(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

}
