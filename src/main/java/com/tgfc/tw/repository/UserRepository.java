package com.tgfc.tw.repository;

import com.tgfc.tw.entity.po.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
}
