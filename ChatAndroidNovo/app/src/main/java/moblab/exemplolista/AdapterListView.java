package moblab.exemplolista;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by charles on 15/07/16.
 */
public class AdapterListView extends BaseAdapter {

    private List<ItemListView> listaItens;
    private LayoutInflater inflater;

    public AdapterListView(Context contexto, List<ItemListView> listaItens) {
        this.listaItens = listaItens;
        this.inflater = LayoutInflater.from(contexto);
    }

    @Override
    public int getCount() {
        return listaItens.size();
    }

    @Override
    public Object getItem(int posicao) {
        return listaItens.get(posicao);
    }

    @Override
    public long getItemId(int posicao) {
        return posicao;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ItemSuporte itemSuporte;

        if (view == null) {
            view = inflater.inflate(R.layout.lista_itens, null);

            itemSuporte = new ItemSuporte();
            itemSuporte.nomeView = ((TextView) view.findViewById(R.id.item_nome));
            itemSuporte.textoView = ((TextView) view.findViewById(R.id.item_texto));

            view.setTag(itemSuporte);
        }
        else {
            itemSuporte = (ItemSuporte) view.getTag();
        }

        ItemListView item = listaItens.get(i);
        itemSuporte.nomeView.setText(item.getNome());
        itemSuporte.textoView.setText(item.getTexto());

        return view;
    }

    private class ItemSuporte {
        public TextView textoView;
        public TextView nomeView;
    }
}
