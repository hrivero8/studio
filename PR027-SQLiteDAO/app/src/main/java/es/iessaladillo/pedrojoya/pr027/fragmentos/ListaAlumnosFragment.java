package es.iessaladillo.pedrojoya.pr027.fragmentos;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import es.iessaladillo.pedrojoya.pr027.R;
import es.iessaladillo.pedrojoya.pr027.adaptadores.AlumnosAdapter;
import es.iessaladillo.pedrojoya.pr027.bd.Dao;
import es.iessaladillo.pedrojoya.pr027.bd.DbContract;
import es.iessaladillo.pedrojoya.pr027.modelos.Alumno;
import es.iessaladillo.pedrojoya.pr027.utils.HidingScrollListener;
import es.iessaladillo.pedrojoya.pr027.utils.SimpleCursorLoader;

public class ListaAlumnosFragment extends Fragment implements AlumnosAdapter
        .OnItemLongClickListener, ActionMode.Callback, AlumnosAdapter.OnItemClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String STATE_LISTA = "state_lista";
    private static final int DATOS_LOADER = 0;

    private TextView lblNuevoAlumno;
    private AlumnosAdapter mAdaptador;
    private ActionMode mActionMode;
    private HidingScrollListener mHidingScrollListener;
    private Dao mDao;
    private LinearLayoutManager mLayoutManager;
    private Parcelable mEstadoLista;
    private RecyclerView.AdapterDataObserver mObservador;

    // Interfaz de comunicación con la actividad.
    public interface OnListaAlumnosFragmentListener {

        void onAgregarAlumno();

        void onEditarAlumno(long id);

        void onConfirmarEliminarAlumnos();

        void onShowFAB();

        void onHideFAB();

    }

    private OnListaAlumnosFragmentListener listener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lista_alumnos, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initVistas(getView());
        mDao = Dao.getInstance(getActivity());
        getActivity().getSupportLoaderManager().initLoader(DATOS_LOADER, null, this);
    }

    // Obtiene e inicializa las vistas.
    private void initVistas(View v) {
        lblNuevoAlumno = (TextView) v.findViewById(R.id.lblNuevoAlumno);
        lblNuevoAlumno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onAgregarAlumno();
            }
        });
        RecyclerView lstAlumnos = (RecyclerView) v.findViewById(R.id.lstAlumnos);
        lstAlumnos.setHasFixedSize(true);
        mAdaptador = new AlumnosAdapter();
        mAdaptador.setOnItemClickListener(this);
        mAdaptador.setOnItemLongClickListener(this);
        mObservador = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                checkAdapterIsEmpty();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                checkAdapterIsEmpty();
            }
        };
        mAdaptador.registerAdapterDataObserver(mObservador);
        lstAlumnos.setAdapter(mAdaptador);
        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,
                false);
        lstAlumnos.setLayoutManager(mLayoutManager);
        lstAlumnos.addItemDecoration(
                new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        lstAlumnos.setItemAnimator(new DefaultItemAnimator());
        // Drag & drop y Swipe to dismiss.
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                        ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(RecyclerView recyclerView,
                            RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                        // Se elimina el elemento.
                        eliminarAlumno(viewHolder.getAdapterPosition());
                    }
                });
        itemTouchHelper.attachToRecyclerView(lstAlumnos);
        // Hide / show FAB on scrolling.
        mHidingScrollListener = new HidingScrollListener() {

            @Override
            public void onHide() {
                listener.onHideFAB();
            }

            @Override
            public void onShow() {
                listener.onShowFAB();
            }
        };
        lstAlumnos.addOnScrollListener(mHidingScrollListener);
        // Tras asignar el adaptador se realiza la comprobación inicial.
        checkAdapterIsEmpty();
    }

    private void checkAdapterIsEmpty() {
        lblNuevoAlumno.setVisibility(mAdaptador.getItemCount() == 0 ? View.VISIBLE : View
                .INVISIBLE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Se salva el estado del RecyclerView.
        mEstadoLista = mLayoutManager.onSaveInstanceState();
        outState.putParcelable(STATE_LISTA, mEstadoLista);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Se retaura el estado de la lista.
        if (mEstadoLista != null) {
            mLayoutManager.onRestoreInstanceState(mEstadoLista);
        }
    }

    // Crea el adaptador y carga la lista de alumnos.
    public void cargarAlumnos() {
        // Se obtienen los datos de los alumnos a través del Dao.
        ArrayList<Alumno> alumnos = (ArrayList<Alumno>) (mDao.getAllAlumnos());
        mAdaptador.swapData(alumnos);
    }

    // Cuando el fragmento es cargado en la actividad.
    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            // Se establece la actividad como objeto listener.
            listener = (OnListaAlumnosFragmentListener) activity;
        } catch (ClassCastException e) {
            // La actividad no implementa la interfaz.
            throw new ClassCastException(
                    activity.toString() + " debe implementar OnElementoSeleccionadoListener");
        }
    }

    // Elimina de la base de datos los alumnos seleccionados, actualiza el
    // adaptador y cierra el modo de acción conextual.
    public void eliminarAlumnosSeleccionados() {
        // Se obtiene el array con las posiciones seleccionadas.
        ArrayList<Integer> seleccionados = mAdaptador.getSelectedItemsPositions();
        // Por cada selección.
        for (int i = 0; i < seleccionados.size(); i++) {
            // Se obtiene el alumo.
            Alumno alu = mAdaptador.getItemAtPosition(seleccionados.get(i));
            // Se elimina el alumo de la bd.
            mDao.deleteAlumno(alu.getId());
        }
        // Se eliminan del adaptador.
        mAdaptador.removeSelectedItems();
        // Se finaliza el modo contextual.
        mActionMode.finish();
    }

    private void eliminarAlumno(int position) {
        // Se obtiene el alumo.
        Alumno alu = mAdaptador.getItemAtPosition(position);
        // Se borra de la base de datos.
        if (mDao.deleteAlumno(alu.getId())) {
            // Si se ha eliminado de la bd se borra del adaptador.
            mAdaptador.removeItem(position);
        }
    }

    @Override
    public void onItemClick(View view, Alumno alumno, int position) {
        if (mActionMode != null) {
            toggleSelection(position);
        } else {
            // Se da la orden de editar.
            listener.onEditarAlumno(alumno.getId());
        }
    }

    @Override
    public void onItemLongClick(View view, Alumno alumno, int position) {
        if (mActionMode != null) {
            toggleSelection(position);
        } else {
            mActionMode = getActivity().startActionMode(this);
            toggleSelection(position);
            listener.onHideFAB();
        }
    }

    // Cambia el estado de selección del elemento situado en dicha posición.
    private void toggleSelection(int position) {
        // Se cambia el estado de selección
        mAdaptador.toggleSelection(position);
        // Se actualiza el texto del action mode contextual.
        mActionMode.setTitle(mAdaptador.getSelectedItemCount() + " / " + mAdaptador.getItemCount());
        // Si ya no hay ningún elemento seleccionado se finaliza el modo de
        // acción contextual
        if (mAdaptador.getSelectedItemCount() == 0) {
            mActionMode.finish();
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        // Se infla la especificación del menú contextual en el
        // menú.
        actionMode.getMenuInflater().inflate(R.menu.fragment_lista_alumnos, menu);
        // Se retorna que ya se ha gestionado el evento.
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        // Dependiendo del elemento pulsado.
        switch (menuItem.getItemId()) {
            case R.id.mnuAlumnoEliminar:
                // Si hay elementos seleccionados se pide confirmación.
                if (mAdaptador.getSelectedItemCount() > 0) {
                    // Se almacena el modo contextual para poder cerrarlo
                    // una vez eliminados.
                    mActionMode = actionMode;
                    // Se pide confirmación.
                    listener.onConfirmarEliminarAlumnos();
                }
                break;
        }
        // Se retorna que se ha procesado el evento.
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        mAdaptador.clearSelections();
        mActionMode = null;
        listener.onShowFAB();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mActionMode != null) {
            mActionMode.finish();
        }
        mHidingScrollListener.reset();
    }

    @Override
    public void onDestroy() {
        // Se quita el registro el observador.
        mAdaptador.unregisterAdapterDataObserver(mObservador);
        super.onDestroy();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new SimpleCursorLoader(getActivity()) {
            @Override
            protected Cursor getCursor() {
                return mDao.queryAllAlumnos();
            }

            @Override
            protected Uri getUri() {
                return Uri.parse(DbContract.Alumno.URI);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null) {
            mAdaptador.swapData(Dao.cursorToAlumnos(data));
            checkAdapterIsEmpty();
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdaptador.swapData(null);
    }

    @Override
    public void onDestroyView() {
        mDao.closeDatabase();
        super.onDestroyView();
    }

}
