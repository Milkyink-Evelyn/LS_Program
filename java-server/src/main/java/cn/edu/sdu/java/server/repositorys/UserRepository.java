package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUserName(String userName);

    Optional<User> findByPersonNum(String perNum);

    Optional<User> findByPersonPersonId(Integer personId);

    Boolean existsByUserName(String userName);

    /**
     * 按角色名查询用户，例如 ROLE_ADMIN、ROLE_PATIENT。
     */
    List<User> findByUserType_NameIn(Collection<String> names);

    List<User> findAllByOrderByPersonIdAsc();

    @Query(value = "select count(*) from User where lastLoginTime > ?1")
    Integer countLastLoginTime(String date);

    @Query(value = "select userType.id, count(personId) from User group by userType.id")
    List<?> getCountList();
}
