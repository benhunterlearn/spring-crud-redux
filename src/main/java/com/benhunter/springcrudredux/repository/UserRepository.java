package com.benhunter.springcrudredux.repository;

import com.benhunter.springcrudredux.model.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
    User findUserByEmail(String email);
}
