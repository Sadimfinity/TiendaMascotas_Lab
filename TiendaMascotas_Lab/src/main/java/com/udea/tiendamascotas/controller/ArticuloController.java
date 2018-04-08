package com.udea.tiendamascotas.controller;

import com.udea.tiendamascotas.entity.Articulo;
import com.udea.tiendamascotas.controller.util.JsfUtil;
import com.udea.tiendamascotas.controller.util.PaginationHelper;
import com.udea.tiendamascotas.ejb.ArticuloFacade;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.List;
import java.util.ResourceBundle;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.naming.NamingException;
import org.primefaces.model.UploadedFile;

@Named("articuloController")

@SessionScoped
public class ArticuloController implements Serializable {

    private Articulo current;
    private DataModel items = null;
    @EJB
    private com.udea.tiendamascotas.ejb.ArticuloFacade ejbFacade;
    private PaginationHelper pagination;
    private int selectedItemIndex;
    private List<Articulo> allArticulos;
    private UploadedFile uploadedFile;
    private UploadedFile foto;

    
        public ArticuloController() {
    }
    public UploadedFile getFoto() {
        return foto;
    }

    public List<Articulo> getAllArticulos() {
        if (allArticulos.isEmpty()) {
            allArticulos = getFacade().findAll();
        }
        return allArticulos;
    }

        public UploadedFile getUploadedFile() {
        return uploadedFile;
    }

    public Articulo getSelected() {
        if (current == null) {
            current = new Articulo();
            selectedItemIndex = -1;
        }
        return current;
    }

    private ArticuloFacade getFacade() {
        return ejbFacade;
    }

    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(10) {

                @Override
                public int getItemsCount() {
                    return getFacade().count();
                }

                @Override
                public DataModel createPageDataModel() {
                    return new ListDataModel(getFacade().findRange(new int[]{getPageFirstItem(), getPageFirstItem() + getPageSize()}));
                }
            };
        }
        return pagination;
    }

    public String prepareList() {
        recreateModel();
        return "List";
    }

    public String prepareView() {
        current = (Articulo) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareCreate() {
        current = new Articulo();
        selectedItemIndex = -1;
        return "Create";
    }
    
    
    
    public String create() throws IOException, NamingException {
        try {
            try{
            String especie = current.getEspecie();
            String raza = current.getRaza();
            int edad = current.getEdad();
            if (especie.equals("")) {
                current.setEspecie(null);
            }
            if (raza.equals("")) {
                current.setRaza(null);
            }
            if (String.valueOf(edad).equals("")) {
                current.setEdad(0);
            }
            current.setFoto("A random path");
            getFacade().create(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ArticuloCreated"));
            return prepareCreate();
            } catch (NullPointerException e){
                return null;
            }
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    
    public String prepareEdit() {
        current = (Articulo) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String update() {
        try {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ArticuloUpdated"));
            return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        current = (Articulo) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        performDestroy();
        recreatePagination();
        recreateModel();
        return "List";
    }

    public String destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return "View";
        } else {
            // all items were removed - go back to list
            recreateModel();
            return "List";
        }
    }

    private void performDestroy() {
        try {
            getFacade().remove(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ArticuloDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    private void updateCurrentItem() {
        int count = getFacade().count();
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count - 1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = getFacade().findRange(new int[]{selectedItemIndex, selectedItemIndex + 1}).get(0);
        }
    }

    public DataModel getItems() {
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        return items;
    }

    private void recreateModel() {
        items = null;
    }

    private void recreatePagination() {
        pagination = null;
    }

    public String next() {
        getPagination().nextPage();
        recreateModel();
        return "List";
    }

    public String previous() {
        getPagination().previousPage();
        recreateModel();
        return "List";
    }

    public SelectItem[] getItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), false);
    }

    public SelectItem[] getItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), true);
    }

    public Articulo getArticulo(java.lang.Long id) {
        return ejbFacade.find(id);
    }

    @FacesConverter(forClass = Articulo.class)
    public static class ArticuloControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ArticuloController controller = (ArticuloController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "articuloController");
            return controller.getArticulo(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            key = Long.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Articulo) {
                Articulo o = (Articulo) object;
                return getStringKey(o.getId_articulo());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Articulo.class.getName());
            }
        }

    }

}
