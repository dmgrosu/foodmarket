package md.ramaiana.foodmarket.testservices;

import md.ramaiana.foodmarket.domain.client.data.ClientEntity;
import md.ramaiana.foodmarket.domain.client.data.ClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TestClientService {

  private final ClientRepository clientRepository;
  private int counter = 0;

  public TestClientService(ClientRepository clientRepository) {
    this.clientRepository = clientRepository;
  }

  @Transactional
  public ClientEntity create() {
    counter++;
    return create("Test Client " + counter, "IDNO" + String.format("%09d", counter));
  }

  @Transactional
  public ClientEntity create(String name, String idno) {
    var client = new ClientEntity();
    client.setName(name);
    client.setIdno(idno);
    return clientRepository.save(client);
  }
}
