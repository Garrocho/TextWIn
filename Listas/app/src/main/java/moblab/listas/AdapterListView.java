package moblab.listas;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by moblab on 15/07/16.
 */
public class AdapterListView extends BaseAdapter {

    public LayoutInflater mInflater;
    public List<ItemListView> itens;

    public AdapterListView(Context context, List<ItemListView> itens) {
        this.itens = itens;
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return itens.size();
    }

    @Override
    public Object getItem(int i) {
        return itens.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ItemSuporte itemHolder;

        if (view == null) {
            //infla o layout para podermos pegar as views
            view = mInflater.inflate(R.layout.item_lista, null);

            //cria um item de suporte para não precisarmos sempre
            //inflar as mesmas informacoes
            itemHolder = new ItemSuporte();
            itemHolder.tituloTexto = ((TextView) view.findViewById(R.id.item_texto));

            //define os itens na view;
            view.setTag(itemHolder);
        }
        else {
            //se a view já existe pega os itens.
            itemHolder = (ItemSuporte) view.getTag();
        }
        //pega os dados da lista
        //e define os valores nos itens.
        ItemListView item = itens.get(i);
        itemHolder.tituloTexto.setText(item.getTexto());

        //retorna a view com as informações
        return view;
    }

    private class ItemSuporte {
        TextView tituloTexto;
    }
}
