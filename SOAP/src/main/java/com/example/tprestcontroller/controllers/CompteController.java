package com.example.tprestcontroller.controllers;

import com.example.tprestcontroller.entities.Compte;
import com.example.tprestcontroller.entities.TypeCompte;
import com.example.tprestcontroller.repositories.CompteRepository;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Component
@WebService(serviceName = "BanqueWS")
public class CompteController {

    @Autowired
    private CompteRepository compteRepository;


    @WebMethod
    public List<Compte> getAllComptes() {
        return compteRepository.findAll();
    }

    @WebMethod
    public Compte getCompteById(@WebParam(name = "id") Long id) {
    return compteRepository.findById(id).orElse(null);
    }

    @WebMethod(operationName = "createCompte")
    public Compte createCompte(@WebParam(name = "solde") Double solde,
                               @WebParam(name = "typeCompte") TypeCompte type) {
        Compte compte = new Compte(solde, type);
        return compteRepository.save(compte);
    }



    @WebMethod
    public boolean deleteCompte(@WebParam(name = "id") Long id) {
        if (compteRepository.existsById(id)) {
            compteRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
