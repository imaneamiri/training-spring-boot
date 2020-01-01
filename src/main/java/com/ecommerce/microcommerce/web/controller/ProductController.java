package com.ecommerce.microcommerce.web.controller;

import com.ecommerce.microcommerce.dao.ProductDao;
import com.ecommerce.microcommerce.model.Product;
import com.ecommerce.microcommerce.web.exceptions.ProduitIntrouvableException;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;



@Api(value = "/Produits", description="API pour les opérations CRUD sur les produits.")
@RestController
public class ProductController {

    @Autowired
    private ProductService productService;


    //Récupérer la liste des produits

    @RequestMapping(value = "/Produits", method = RequestMethod.GET)
    @ApiOperation(
            value = "Récupère la liste de tous les produits.",
            notes = "Attention, la récupération de la liste des produits est soumise à des droits d'accès.",
            response = Product.class,
            responseContainer = "List"
    )
    @ApiResponses( value  = {
            @ApiResponse ( code = 200, message = "La liste des produits a été retrouvé avec succès."),
            @ApiResponse( code = 401, message = "La consultation de la liste des produits n'est pas autorisé."),
            @ApiResponse( code = 403, message = "La consultation de la liste des produits est interdite."),
            @ApiResponse( code = 404, message  = "La liste des produits est introuvable.")
    })
    public MappingJacksonValue listeProduits() {

        Iterable<Product> produits = this.productService.getProductDao().findAll();

        SimpleBeanPropertyFilter monFiltre = SimpleBeanPropertyFilter.serializeAllExcept("prixAchat");

        FilterProvider listDeNosFiltres = new SimpleFilterProvider().addFilter("monFiltreDynamique", monFiltre);

        MappingJacksonValue produitsFiltres = new MappingJacksonValue(produits);

        produitsFiltres.setFilters(listDeNosFiltres);

        return produitsFiltres;
    }


    //Récupérer un produit par son Id
    @GetMapping(value = "/Produits/{id}")
    @ApiOperation(
            value = "Récupère un produit grâce à son ID à condition que celui-ci soit en stock!",
            notes = "Attention, la récupération du produit est soumise à des droits d'accès.",
            response = Product.class
    )
    @ApiResponses( value  = {
            @ApiResponse ( code  =  200, message  =  "La récupération du produit  a été effectuée avec succès."),
            @ApiResponse( code  =  401 , message  =  "La récupération du produit est non autorisée" ),
            @ApiResponse( code  =  403 , message  =  " La récupération du produit  est interdite." ),
            @ApiResponse( code  =  404 , message  =  " Le produit est introuvable.")
    })
    public Product afficherUnProduit(@ApiParam(value = "L'id du produit à afficher.", required = true) @PathVariable int id) {

        Product produit = this.productService.getProductDao().findById(id);

        if(produit==null) throw new ProduitIntrouvableException("Le produit avec l'id " + id + " est INTROUVABLE. Écran Bleu si je pouvais.");

        return produit;
    }




    //ajouter un produit
    @PostMapping(value = "/Produits")
    @ApiOperation(
            value = "Ajoute un produit .",
            notes = "Attention, l'ajout d'un produit est soumis à des droits d autorisation.",
            response = Product.class

    )
    @ApiResponses( value  = {
            @ApiResponse ( code  =  200, message  =  "L'ajout du produit  a été effectuée avec succès."),
            @ApiResponse( code  =  201 , message  =  "Le produit est créé."),
            @ApiResponse( code  =  401 , message  =  "L'ajout du produit est non autorisé" ),
            @ApiResponse( code  =  403 , message  =  "L'ajout du produit est interdit." ),
            @ApiResponse( code  =  404 , message  =  " Le produit est introuvable ." )
    })
    public ResponseEntity<Product> ajouterProduit(
            @Valid
            @RequestBody
            @ApiParam(value = "Le produit à ajouter.", required = true) Product product) {
        Product productAdded =  this.productService.getProductDao().save(product);

        if (product.getPrix() == 0) {
            throw new ProduitGratuitException();
        }

        if (productAdded == null)
            return ResponseEntity.noContent().build();

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(productAdded.getId())
                .toUri();

        return ResponseEntity.created(location).body(productAdded);
    }

    @DeleteMapping (value = "/Produits/{id}")
    @ApiOperation (
            httpMethod  =  "DELETE" ,
            value  =  "Supprime un produit.")
    @ApiResponses( value  = {
            @ApiResponse ( code  =  200 , message  =  "La suprression a été effectuée avec succès." ),
            @ApiResponse( code  =  204 , message  =  "Le produit a supprimer est introuvable."),
            @ApiResponse( code  =  401 , message  =  "Droit insuffisant pour supprimer ce produit." ),
            @ApiResponse( code  =  403 , message  =  "La suppression de ce produit est interdite." )
    })
    public void supprimerProduit(@ApiParam(value = "L'id du produit à supprimer.", required = true) @PathVariable int id) {

        this.productService.getProductDao().delete(id);
    }

    @PutMapping (value = "/Produits")
    @ApiOperation(
            value = "Fait la mise à jour du produit.",
            notes = "Attention, la mise à jour  est soumise à des droits d'accès.",
            response = Product.class
    )
    @ApiResponses( value  = {
            @ApiResponse ( code  =  200 , message  =  "La mise à jour du produit  a été effectuée avec succès." ),
            @ApiResponse( code  =  201 , message  =  "Le produit a été créé."),
            @ApiResponse( code  =  401 , message  =  "la mise à jour n'est pas autorisée." ),
            @ApiResponse( code  =  403 , message  =  "La mise à jour du produit  est interdite." ),
            @ApiResponse( code  =  404 , message  =  "Le produit est introuvable ." )
    })
    public void updateProduit(
            @RequestBody
            @ApiParam(value = "Le produit à mettre à jour.", required = true) Product product) {
        if (product.getPrix() == 0) {
            throw new ProduitGratuitException();
        }
        this.productService.getProductDao().save(product);
    }


    //Pour les tests
    @GetMapping(value = "test/produits/{prix}")
    public List<Product>  testeDeRequetes(@PathVariable int prix) {

        return this.productService.getProductDao().chercherUnProduitCher(400);
    }

    @GetMapping(value = "/AdminProduits")
    public HashMap<String,Integer> afficherMargeProduits() {
        return this.productService.calculerMargeProduit();
    }

    @GetMapping(value = "/ProduitsAlphabetique")
    @ApiOperation(
            value = "Trie les produits par ordre alphabétique.",
            notes = "Attention, le tri des produits est soumis à des droits d'accès.",
            response = Product.class,
            responseContainer = "List"
    )
    @ApiResponses( value  = {
            @ApiResponse ( code  =  200, message  =  "Le tri des produits  par ordre alphabéthique   a été effectuée avec succès."),
            @ApiResponse( code  =  401 , message  =  "Le tri des produits par ordre alphabétique est non autorisé" ),
            @ApiResponse( code  =  403 , message  =  " Le tri des produits par ordre alphabétique est interdit." ),
            @ApiResponse( code  =  404 , message  =  " Les produits à trier ne sont pas trouvés." )
    })
    public List<Product> trierParOrdreAlphabetique() {
        //productDao.findAll(new Sort(Sort.Direction.ASC,nom);
        return this.productService.getProductDao().findAllAlphabetically();
    }


}
