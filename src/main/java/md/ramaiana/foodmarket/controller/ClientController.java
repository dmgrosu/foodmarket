package md.ramaiana.foodmarket.controller;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import lombok.extern.slf4j.Slf4j;
import md.ramaiana.foodmarket.model.Client;
import md.ramaiana.foodmarket.proto.Clients;
import md.ramaiana.foodmarket.proto.Common;
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
    private final JsonFormat.Printer printer;

    @Autowired
    public ClientController(ClientService clientService) {
        this.clientService = clientService;
        this.printer = JsonFormat.printer().omittingInsignificantWhitespace();
    }

    @GetMapping("/findByIdno")
    public ResponseEntity<?> getIdByIdno(@RequestParam("idno") String idno) throws InvalidProtocolBufferException {
        try {
            Client client = clientService.findClientByIdno(idno);
            return ResponseEntity.ok(printer.print(buildProtoFromDomain(client)));
        } catch (ClientNotFoundException e) {
            log.warn(e.getMessage());
            return ResponseEntity.ok(printer.print(buildNotFoundResponse(e.getMessage())));
        }
    }

    private Clients.Client buildProtoFromDomain(Client client) {
        return Clients.Client.newBuilder()
                .setId(client.getId())
                .setName(client.getName())
                .setIdno(client.getIdno())
                .build();
    }

    private Common.ErrorResponse buildNotFoundResponse(String error) {
        return Common.ErrorResponse.newBuilder()
                .addErrors(Common.Error.newBuilder()
                        .setCode(Common.ErrorCode.CLIENT_NOT_FOUND)
                        .setDescription(error))
                .build();
    }

}
