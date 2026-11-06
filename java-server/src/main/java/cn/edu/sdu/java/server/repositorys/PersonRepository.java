package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person, Integer> {
    Optional<Person> findByNum(String num);

    boolean existsByNum(String num);

    @Query("select p.photo from Person p where p.personId = ?1")
    byte[] getPhotoByPersonId(Integer personId);
}
