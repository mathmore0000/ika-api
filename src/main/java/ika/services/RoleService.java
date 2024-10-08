package ika.services;

import ika.entities.Role;
import ika.repositories.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    // Método para buscar todos os roles
    public Iterable<Role> findAllRoles() {
        return roleRepository.findAll();
    }

    // Método para buscar role por nome
    public Optional<Role> findRoleByName(String roleName) {
        return roleRepository.findById(roleName);
    }
}
