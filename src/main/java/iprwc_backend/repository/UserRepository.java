package iprwc_backend.repository;

import iprwc_backend.entity.User;
import iprwc_backend.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by email (for login)
    Optional<User> findByEmail(String email);

    // Check if email exists (for registration validation)
    boolean existsByEmail(String email);

    // Find users by role
    List<User> findByRole(UserRole role);

    // Count users by role
    long countByRole(UserRole role);
}