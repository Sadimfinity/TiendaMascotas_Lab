/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.udea.tiendamascotas.ejb;

import com.udea.tiendamascotas.entity.Articulo;
import java.util.List;
import javax.annotation.PreDestroy;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 *
 * @author Gaviria Zapata
 */
@Stateless
public class ArticuloFacade extends AbstractFacade<Articulo> {

    @PersistenceContext(unitName = "com.udea_TiendaMascotas_Lab_war_1.0-SNAPSHOTPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ArticuloFacade() {
        super(Articulo.class);
    }
    
        
    public List<Articulo> findAllArticulos(){
        Query query = em.createNamedQuery("Articulo.findAll");
        return (List<Articulo>) query.getResultList();
    }    
    
    public Articulo findArticuloById(long id){
        Query query = em.createNamedQuery("Articulo.findByIdArticulo");
        query.setParameter("id_articulo", id);
        return (Articulo) query.getSingleResult();
    }


    @PreDestroy
    public void destruct() {
        em.close();
    }

}
