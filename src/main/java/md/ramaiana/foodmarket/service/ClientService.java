package md.ramaiana.foodmarket.service;

import md.ramaiana.foodmarket.dao.ClientDao;
import md.ramaiana.foodmarket.model.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Dmitri Grosu (dmitri.grosu@gmail.com), 2/7/21
 */
@Service
public class ClientService {

    private final ClientDao clientDao;

    @Autowired
    public ClientService(ClientDao clientDao) {
        this.clientDao = clientDao;
    }

    public Client findClientByIdno(String idno) throws ClientNotFoundException {
        return clientDao.findByIdnoAndDeleted_atIsNull(idno)
                .orElseThrow(() -> new ClientNotFoundException(String.format("Client with IDNO [%s] not found", idno)));
    }

}
