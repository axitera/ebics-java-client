package de.axitera.ebics.client.repository.filebased;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.axitera.ebics.client.logging.IEbicsLogger;
import de.axitera.ebics.client.repository.IEbicsDataRepository;
import org.kopi.ebics.client.User;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A Simple implementation that write values to disk using jackson.
 * @param <T>
 */

public abstract class AbstractFileBasedRepo<T> implements IEbicsDataRepository<T> {

    ObjectMapper objectMapper = new ObjectMapper();
    final File folder;
    IEbicsLogger logger;

    protected AbstractFileBasedRepo(File folder) {
        if(!folder.isDirectory() ||!folder.canWrite()){
            throw new IllegalArgumentException("Folder must be a directory with write permissions");
        }
        this.folder = folder;
    }

    abstract Class<T> getClassOfT();
    abstract String getUniqueFileNameForData(T data);



    @Override
    public void put(T data) {
        File f = new File(folder,getUniqueFileNameForData(data));
        writeData(data,f);
    }

    @Override
    public List<T> getAll() {
        ArrayList<T> data = new ArrayList<T>();
        for(File f: folder.listFiles()){
            if(!f.isDirectory()){
                data.add(readData(f));
            }
        }
        return data;
    }

    @Override
    public List<String> getAllIds() {
        return Arrays.stream(folder.listFiles())
                .filter(f->!f.isDirectory())
                .map(File::getName)
                .collect(Collectors.toList());
    }

    @Override
    public T getById(String id) {
        Optional<T> data = Arrays.stream(folder.listFiles())
                .filter(f->!f.isDirectory())
                .filter(f->f.getName().equals(id))
                .map(f->readData(f))
                .findFirst();

        if(data.isPresent()){
            return data.get();
        }
        logger.warn("No Data found for id>>"+id+"<< in folder>>"+folder.getAbsolutePath()+"<<");
        return null;
    }

    protected void writeData(T data, File file) {
        try{
            objectMapper.writeValue(file, data);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected T readData(File file) {
        try{
            return objectMapper.readValue(file,getClassOfT());
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


}
