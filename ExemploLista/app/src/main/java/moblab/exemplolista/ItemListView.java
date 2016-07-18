package moblab.exemplolista;

/**
 * Created by charles on 15/07/16.
 */
public class ItemListView {

    private String texto;

    public ItemListView() {
        this.texto = "";
    }

    public ItemListView(String texto) {
        this.texto = texto;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }
}
