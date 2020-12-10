package platform;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface CodeRepository extends JpaRepository<Code, UUID> {}