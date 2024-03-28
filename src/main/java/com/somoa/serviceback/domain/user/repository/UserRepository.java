package com.somoa.serviceback.domain.user.repository;

import com.somoa.serviceback.domain.order.dto.OrderWithGroupnameResponse;
import com.somoa.serviceback.domain.user.entity.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.List;

public interface UserRepository extends ReactiveCrudRepository<User, Integer> {

    Mono<User> findByUsername(String username);


}
