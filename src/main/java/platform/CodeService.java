package platform;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional

public class CodeService {

  @Autowired
  private CodeRepository codeRepository;

  public Optional<Code> findCode(UUID id){
    return codeRepository.findById(id);
  }

  public List<Code> findCode(){
    return codeRepository.findAll();
  }

  public Code save(Code code) {
    return codeRepository.save(code);
  }

  public Long count() {
    return codeRepository.count();
  }

  public void delete(UUID id) {
    codeRepository.deleteById(id);
  }
}