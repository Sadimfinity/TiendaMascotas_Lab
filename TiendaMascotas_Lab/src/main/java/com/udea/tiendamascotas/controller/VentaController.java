package com.udea.tiendamascotas.controller;

import com.udea.tiendamascotas.entity.Venta;
import com.udea.tiendamascotas.controller.util.JsfUtil;
import com.udea.tiendamascotas.controller.util.PaginationHelper;
import com.udea.tiendamascotas.ejb.VentaFacade;
import com.udea.tiendamascotas.entity.Articulo;
import com.udea.tiendamascotas.ejb.ArticuloFacade;
import java.io.Serializable;
import java.util.LinkedList;
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

@Named("ventaController")
@SessionScoped
public class VentaController implements Serializable {

    @EJB
    private ArticuloFacade articuloFacade;
    private Venta current;
    private DataModel items = null;
    @EJB
    private com.udea.tiendamascotas.ejb.VentaFacade ejbFacade;
    private PaginationHelper pagination;
    private int selectedItemIndex;
    private String idArticulosComprados;

    public VentaController() {
    }

    public String getIdArticulosComprados() {
        return idArticulosComprados;
    }

    public void setIdArticulosComprados(String idArticulosComprados) {
        this.idArticulosComprados = idArticulosComprados;
    }

    public Venta getSelected() {
        if (current == null) {

            current = new Venta();
            selectedItemIndex = -1;
        }
        return current;
    }

    private VentaFacade getFacade() {
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
        current = (Venta) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareCreate() {
        current = new Venta();
        selectedItemIndex = -1;
        return "Create";
    }

    public String create() {
        try {
            String json = getIdArticulosComprados();
            String[] conjuntoIds = json.split(",");
            List listaIds = new LinkedList();
            int idEntrante;
            long id;
            for (int i = 0; i < conjuntoIds.length; i++) {
                idEntrante = Integer.valueOf(conjuntoIds[i]);
                id = (long) idEntrante;
                listaIds.add(i, id);
            }
            current.setArticulosComprados(listaIds);
            getFacade().create(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("VentaCreated"));
            return prepareCreate();
        } catch (NumberFormatException e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String update() {
        try {
            String json = getIdArticulosComprados();
            json = json.replace("[", "");
            json = json.replace("]", "");
            String[] conjuntoIds = json.split(",");
            List listIds = new LinkedList();
            int idEntrante;
            long id;
            for (int i = 0; i < conjuntoIds.length; i++) {
                idEntrante = Integer.valueOf(conjuntoIds[i]);
                id = (long) idEntrante;
                listIds.add(i, id);
            }
            current.setArticulosComprados(listIds);
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("VentaUpdated"));
            return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit() {
        current = (Venta) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String destroy() {
        current = (Venta) getItems().getRowData();
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
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("VentaDeleted"));
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

    public List getarticulos() {
        return current.getArticulosComprados();
    }

    public String getNombreArticulosComprados(List idArticulosComprados) {
        String nombreArticulosComprados = "";
        long id;
        Articulo art;
        for (int i = 0; i < idArticulosComprados.size(); i++) {
            id = (long) idArticulosComprados.get(i);
            art = articuloFacade.findArticuloById(id);
            nombreArticulosComprados = nombreArticulosComprados + art.getNombre() + ", ";
        }
        return nombreArticulosComprados;
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

    public Venta getVenta(java.lang.Long id) {
        return ejbFacade.find(id);
    }

    @FacesConverter(forClass = Venta.class)
    public static class VentaControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            VentaController controller = (VentaController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "ventaController");
            return controller.getVenta(getKey(value));
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
            if (object instanceof Venta) {
                Venta o = (Venta) object;
                return getStringKey(o.getId_venta());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Venta.class.getName());
            }
        }

    }

}
