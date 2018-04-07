package com.udea.tiendamascotas.controller;

import com.udea.tiendamascotas.entity.Factura;
import com.udea.tiendamascotas.controller.util.JsfUtil;
import com.udea.tiendamascotas.controller.util.PaginationHelper;
import com.udea.tiendamascotas.ejb.FacturaFacade;
import com.udea.tiendamascotas.entity.Articulo;
import com.udea.tiendamascotas.entity.Venta;

import java.io.Serializable;
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
import com.udea.tiendamascotas.ejb.ArticuloFacade;
import java.util.Objects;

@Named("facturaController")
@SessionScoped
public class FacturaController implements Serializable {

    private Factura current;
    private DataModel items = null;
    @EJB
    private com.udea.tiendamascotas.ejb.FacturaFacade ejbFacade;
    private PaginationHelper pagination;
    private int selectedItemIndex;
    @EJB
    private ArticuloFacade articuloFacade;

    public FacturaController() {
    }

    public Factura getSelected() {
        if (current == null) {
            current = new Factura();
            selectedItemIndex = -1;
        }
        return current;
    }

    private FacturaFacade getFacade() {
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
        current = (Factura) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareCreate() {
        current = new Factura();
        selectedItemIndex = -1;
        return "Create";
    }

    public String create() {
        try {
            try {
                List<Factura> allFacturas = getFacade().findAll();
                for (Factura allFactura : allFacturas) {
                    if(Objects.equals(current.getId_factura(), allFactura.getId_factura())){
                        return null;
                    }
                }
                List<Articulo> error = current.getVenta().getArticulos();
                if (error == null) {
                    return null;
                } else {
                    current.setPrecioTotal(calcularTotal(current));
                    getFacade().create(current);
                    JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("FacturaCreated"));
                    return prepareCreate();
                }
            } catch (NullPointerException e) {
                JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("CreateFacturaRequiredMessageVenta"));
            }
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
        return null;
    }

    private Double calcularTotal(Factura current) {
        Double total = 0.0;
        long id;
        Articulo article;
        Venta venta = current.getVenta();
        List articulosComprados = venta.getArticulosComprados();
        for (int i = 0; i < articulosComprados.size(); i++) {
            id = (long) articulosComprados.get(i);
            article = articuloFacade.findArticuloById(id);
            total = total + article.getPrecio();
        }
        return total;
    }

    public String prepareEdit() {
        current = (Factura) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String update() {
        try {
            try {
                List<Articulo> error = current.getVenta().getArticulos();
                if (error == null) {
                    return null;
                } else {
                    current.setPrecioTotal(calcularTotal(current));
                    getFacade().edit(current);
                    JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("FacturaUpdated"));
                    return "View";
                }
            } catch (NullPointerException e) {
                JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("CreateFacturaRequiredMessageVenta"));
            }
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
        return null;
    }

    public String destroy() {
        current = (Factura) getItems().getRowData();
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
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("FacturaDeleted"));
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

    public Factura getFactura(java.lang.Long id) {
        return ejbFacade.find(id);
    }

    @FacesConverter(forClass = Factura.class)
    public static class FacturaControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            FacturaController controller = (FacturaController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "facturaController");
            return controller.getFactura(getKey(value));
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
            if (object instanceof Factura) {
                Factura o = (Factura) object;
                return getStringKey(o.getId_factura());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Factura.class.getName());
            }
        }

    }

}
