package com.technews.repository;

import com.technews.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

//repository - data access object (DAO) contains data retrieval, storage, and search capabilities
@Repository
//ensure repository can take User entity model and Id
public interface UserRepository extends JpaRepository<User, Integer> {
//    find by email query
    User findUserByEmail(String email) throws Exception;
}
