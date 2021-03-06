package es.iessaladillo.pedrojoya.pr196.bd;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import es.iessaladillo.pedrojoya.pr196.modelos.Alumno;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Clase de acceso a los datos de la base de datos. Utiliza un objeto de un
 * clase privada interna para gestionar realmente la base de datos, creando
 * hacia el exterior un wrapper (una envoltura) que permita a otros objetos
 * interactuar con la base de datos si hacer uso de sentencias SQL ni conocer
 * detalles internos de ella.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Dao {

    private static Dao sInstance;

    private final DbHelper mHelper; // Ayudante para la creación y gestión de la BD.
    private final ContentResolver mContentResolver;

    // Constructor. Recibe el contexto.
    private Dao(Context contexto) {
        // Se obtiene el mHelper.
        mHelper = DbHelper.getInstance(contexto);
        mContentResolver = contexto.getContentResolver();
    }

    // Retorna la instancia (única) del helper.
    public static synchronized Dao getInstance(Context context) {
        // Al usar el contexto de la aplicación nos aseguramos de que no haya
        // memory leaks si el recibido es el contexto de una actividad.
        if (sInstance == null) {
            sInstance = new Dao(context);
        }
        return sInstance;
    }

    // Abre la base de datos para escritura.
    public SQLiteDatabase openWritableDatabase() {
        return mHelper.getWritableDatabase();
    }

    // Cierra la base de datos.
    public void closeDatabase() {
        mHelper.close();
    }

    // CRUD (Create-Read-Update-Delete) de la tabla alumnos

    // Inserta un alumno en la tabla de alumnmos.
    // Recibe el objeto Alumno a insertar.
    // Retorna el _id del alumna una vez insertado o -1 si se ha producido un
    // error.
    private long createAlumno(Alumno alumno) {
        // Se abre la base de datos.
        SQLiteDatabase bd = mHelper.getWritableDatabase();
        // Se crea la lista de pares campo-valor para realizar la inserción.
        ContentValues valores = alumno.toContentValues();
        // Se realiza el insert
        long resultado = bd.insert(DbContract.Alumno.TABLA, null, valores);
        // Se cierra la base de datos.
        mHelper.close();
        // Se notifica.
        mContentResolver.notifyChange(
                Uri.parse(DbContract.Alumno.URI), null);
        // Se retorna el _id del alumno insertado o -1 si error.
        return resultado;
    }

    public Single<Long> createAlumnoRx(final Alumno alumno) {
        return Single.fromCallable(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return createAlumno(alumno);
            }
        });
    }

    // Borra de la BD un alumno. Recibe el _id del alumno a borrar. Retorna true
    // si se ha realizado la eliminación con éxito.
    public boolean deleteAlumno(long id) {
        // Se abre la base de datos.
        SQLiteDatabase bd = mHelper.getWritableDatabase();
        // Se realiza el delete.
        long resultado = bd.delete(DbContract.Alumno.TABLA, DbContract.Alumno._ID + " = "
                + id, null);
        // Se cierra la base de datos.
        mHelper.close();
        // Se notifica.
        //mContentResolver.notifyChange(
        //        Uri.parse(Instituto.Alumno.URI), null);
        // Se retorna si se ha eliminado algún alumno.
        return resultado > 0;

    }

    // Actualiza en la BD los datos de un alumno. Recibe el alumno. Retorna true
    // si la actualización se ha realizado con éxito.
    private boolean updateAlumno(Alumno alumno) {
        // Se abre la base de datos.
        SQLiteDatabase bd = mHelper.getWritableDatabase();
        // Se crea la lista de pares clave-valor con cada campo-valor.
        ContentValues valores = alumno.toContentValues();
        // Se realiza el update.
        long resultado = bd.update(DbContract.Alumno.TABLA, valores, DbContract.Alumno._ID
                + " = " + alumno.getId(), null);
        // Se cierra la base de datos.
        mHelper.close();
        // Se notifica.
        mContentResolver.notifyChange(
                Uri.parse(DbContract.Alumno.URI), null);
        // Se retorna si ha actualizado algún alumno.
        return resultado > 0;
    }

    public Completable updateAlumnoRx(final Alumno alumno) {
        return Completable.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return updateAlumno(alumno);
            }
        });
    }

    // Consulta en la BD los datos de un alumno. Recibe el _id del alumno a
    // consultar. Retorna el objeto Alumno o null si no existe.
    private Alumno queryAlumno(long id) {
        // Se abre la base de datos.
        SQLiteDatabase bd = mHelper.getWritableDatabase();
        // Se realiza la query SQL sobre la BD.
        Cursor cursor = bd.query(true, DbContract.Alumno.TABLA,
                DbContract.Alumno.TODOS, DbContract.Alumno._ID + " = " + id,
                null, null, null, null, null);
        // Se mueve al primer registro del cursor.
        Alumno alumno = null;
        if (cursor != null) {
            cursor.moveToFirst();
            // Retorno el objeto Alumno correspondiente.
            alumno = cursorToAlumno(cursor);
        }
        // Se cierra la base de datos.
        mHelper.close();
        // Se retorna el alumno o null.
        return alumno;
    }

    public Single<Alumno> queryAlumnoRx(final long id) {
        return Single.fromCallable(new Callable<Alumno>() {
            @Override
            public Alumno call() throws Exception {
                return queryAlumno(id);
            }
        });
    }

    // Consulta en la BD todos los alumnos. Retorna el cursor resultado de la
    // consulta (puede ser null si no hay alumnos), ordenados alfabéticamente
    // por nombre.
    private Cursor queryAllAlumnos() {
        // Se abre la base de datos.
        SQLiteDatabase bd = mHelper.getWritableDatabase();
        // Se realiza la consulta y se retorna el cursor.
        return  bd.query(DbContract.Alumno.TABLA, DbContract.Alumno.TODOS, null,
                null, null, null, DbContract.Alumno.NOMBRE);
    }

    // Consulta en la VD todos los alumnos. Retorna una lista de objeto Alumno,
    // ordenados alfabéticamente por nombre.
    private ArrayList<Alumno> getAllAlumnos() {
        ArrayList<Alumno> lista = new ArrayList<>();
        // Se consultan todos los alumnos en la BD y obtiene un cursor.
        Cursor cursor = this.queryAllAlumnos();
        if (cursor != null) {
            lista = cursorToAlumnos(cursor);
        }
        // Se cierra el cursor (IMPORTANTE).
        if (cursor != null) {
            cursor.close();
        }
        // Se retorna la lista.
        return lista;
    }

    public Single<ArrayList<Alumno>> getAllAlumnosRx() {
        return Single.fromCallable(new Callable<ArrayList<Alumno>>() {
            @Override
            public ArrayList<Alumno> call() throws Exception {
                return getAllAlumnos();
            }
        });
    }

    public static ArrayList<Alumno> cursorToAlumnos(Cursor cursor) {
        ArrayList<Alumno> lista = new ArrayList<>();
        // Se convierte cada registro del cursor en un elemento de la lista.
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Alumno alumno = cursorToAlumno(cursor);
            lista.add(alumno);
            cursor.moveToNext();
        }
        // Se retorna la lista.
        return lista;
    }

    // Crea un objeto Alumno a partir del registro actual de un cursor. Recibe
    // el cursor y retorna un nuevo objeto Alumno cargado con los datos del
    // registro actual del cursor.
    public static Alumno cursorToAlumno(Cursor cursorAlumno) {
        // Crea un objeto Alumno y guarda los valores provenientes
        // del registro actual del cursor.
        Alumno alumno = new Alumno();
        alumno.setId(cursorAlumno.getLong(
                cursorAlumno.getColumnIndexOrThrow(DbContract.Alumno._ID)));
        alumno.setNombre(cursorAlumno.getString(
                cursorAlumno.getColumnIndexOrThrow(DbContract.Alumno.NOMBRE)));
        alumno.setCurso(cursorAlumno.getString(
                cursorAlumno.getColumnIndexOrThrow(DbContract.Alumno.CURSO)));
        alumno.setTelefono(cursorAlumno.getString(
                cursorAlumno.getColumnIndexOrThrow(DbContract.Alumno.TELEFONO)));
        alumno.setDireccion(cursorAlumno.getString(
                cursorAlumno.getColumnIndexOrThrow(DbContract.Alumno.DIRECCION)));
        // Se retorna el objeto Alumno.
        return alumno;
    }

}
